package com.iti.database.psql;

import com.iti.database.ConditionBuilder;
import com.iti.database.DB_Condition;
import com.iti.database.DB_Connection;
import com.iti.database.DB_Handler;
import java.lang.reflect.Field;
import java.util.Map;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PSQL_Handler implements DB_Handler {

    private DataSource dataSource;
    private Connection connection;

    @Override
    public void connect() {
        if (dataSource == null) {
                dataSource = (DataSource)  DB_Connection.getDataSource();
        }
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get a connection from DataSource");
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T> T create(T entity) {
        Class<?> myClass = entity.getClass();
        String tableName = myClass.getSimpleName();
        String columns = "(";
        String placeholder = "(";
        List<Object> values = new ArrayList<>();

        Field[] fields = myClass.getDeclaredFields();
        boolean firstColumn = true;
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            try {
                Object value = fields[i].get(entity);
                if (value != null) {
                    if (!firstColumn) {
                        columns += ", ";
                        placeholder += ", ";
                    } else {
                        firstColumn = false;
                    }
                    columns += fields[i].getName();
                    if (value instanceof PSQLComposite) {
                        PSQLComposite comp = (PSQLComposite) value;
                        placeholder += comp.toRowPlaceHolder();
                        Map<String, Object> compValues = comp.getValues();
                        for (Object compVal : compValues.values()) {
                            values.add(compVal);
                        }
                    } else {
                        placeholder += "?";
                        values.add(value);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + fields[i].getName(), e);
            }
        }
        columns += ")";
        placeholder += ")";

        String query = "INSERT INTO " + tableName + " " + columns + " VALUES " + placeholder + " RETURNING *";
        System.out.println(query);
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) instanceof PSQL_ENUM) {
                    stmt.setObject(i + 1, ((PSQL_ENUM) values.get(i)).getEnumValue(), java.sql.Types.OTHER);
                } else {
                    stmt.setObject(i + 1, values.get(i));
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    for (Field field : fields) {
                        field.setAccessible(true);
                        if (PSQLComposite.class.isAssignableFrom(field.getType())) {
                            String compStr = rs.getString(field.getName());
                            if (compStr != null) {
                                Object compInstance;
                                try {
                                    compInstance = PSQLCompositeHelper.parseComposite(compStr, field.getType());
                                    field.set(entity, compInstance);
                                } catch (ReflectiveOperationException ex) {
                                    Logger.getLogger(PSQL_Handler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        } else {
                            Object dbValue = rs.getObject(field.getName());
                            if (dbValue != null) {
                                try {
                                    if (field.getType().isEnum() && dbValue instanceof String) {
                                        dbValue = Enum.valueOf((Class<Enum>) field.getType(), (String) dbValue);
                                    }
                                    field.set(entity, dbValue);
                                } catch (IllegalAccessException ex) {
                                    Logger.getLogger(PSQL_Handler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getSQLState());
                System.out.println("------------------------------");
                System.out.println(e.getMessage());
                System.out.println("=======================");
                System.out.println(e);
                System.out.println("OOOOOOOOOOOOOOOOO");

                e.printStackTrace();
                System.out.println("XXXXXXXXXXXX");

                throw new RuntimeException("Error inserting entitys", e);
            }

        } catch (SQLException ex) {
            Logger.getLogger(PSQL_Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entity;
    }

    @Override
    public <T> T updateByValue(T entity, String whereColumn, DB_Condition condition, Object conditionValue) {
        Class<?> myClass = entity.getClass();
        String tableName = myClass.getSimpleName();
        String setClause = "";
        List<Object> values = new ArrayList<>();
        Field[] fields = myClass.getDeclaredFields();
        boolean firstField = true;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(entity);
                if (fieldValue != null) {
                    if (!firstField) {
                        setClause += ", ";
                    } else {
                        firstField = false;
                    }
                    setClause += field.getName() + "=";
                    if (fieldValue instanceof PSQLComposite) {
                        PSQLComposite comp = (PSQLComposite) fieldValue;
                        setClause += comp.toRowPlaceHolder();
                        Map<String, Object> compValues = comp.getValues();
                        for (Object compVal : compValues.values()) {
                            values.add(compVal);
                        }
                    } else {
                        setClause += "?";
                        values.add(fieldValue);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }

        String query = "UPDATE " + tableName + " SET " + setClause
                + " WHERE " + whereColumn + " " + condition.getOperator() + " ? RETURNING *";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int idx = 1;
            for (Object val : values) {
                if (val instanceof PSQL_ENUM) {
                    stmt.setObject(idx++, val, java.sql.Types.OTHER);
                } else {
                    stmt.setObject(idx++, val);
                }
            }
            stmt.setObject(idx, conditionValue);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    for (Field field : fields) {
                        field.setAccessible(true);
                        if (PSQLComposite.class.isAssignableFrom(field.getType())) {
                            String compStr = rs.getString(field.getName());
                            if (compStr != null) {
                                Object compInstance;
                                try {
                                    compInstance = PSQLCompositeHelper.parseComposite(compStr, field.getType());
                                    field.set(entity, compInstance);
                                } catch (ReflectiveOperationException ex) {
                                    Logger.getLogger(PSQL_Handler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        } else {
                            Object dbValue = rs.getObject(field.getName());
                            if (dbValue != null) {
                                try {
                                    field.set(entity, dbValue);
                                } catch (IllegalAccessException ex) {
                                    Logger.getLogger(PSQL_Handler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }

                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating entity", e);
        }

        return entity;
    }

    @Override
    public <T> List<T> readAll(Class<T> myClass) {
        List<T> results = new ArrayList<>();
        String tableName = myClass.getSimpleName();
        String query = "SELECT * FROM " + tableName;
        try (PreparedStatement stmt = connection.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            Field[] fields = myClass.getDeclaredFields();
            while (rs.next()) {
                T entity = myClass.getDeclaredConstructor().newInstance();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (PSQLComposite.class.isAssignableFrom(field.getType())) {
                        String compStr = rs.getString(field.getName());
                        if (compStr != null) {
                            Object compInstance = PSQLCompositeHelper.parseComposite(compStr, field.getType());
                            field.set(entity, compInstance);
                        }
                    } else {
                        Object dbValue = rs.getObject(field.getName());
                        if (dbValue != null) {
                            field.set(entity, dbValue);
                        }
                    }
                }
                results.add(entity);
            }
        } catch (SQLException | ReflectiveOperationException e) {
            throw new RuntimeException("Error reading all entities", e);
        }

        return results;
    }

    @Override
    public <T> List<T> readByValue(Class<T> tableClass, String column, DB_Condition condition, Object value) {
        List<T> results = new ArrayList<>();
        String tableName = tableClass.getSimpleName();
        String query = "SELECT * FROM " + tableName + " WHERE " + column + " " + condition.getOperator() + " ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                Field[] fields = tableClass.getDeclaredFields();
                while (rs.next()) {
                    T entity = tableClass.getDeclaredConstructor().newInstance();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        if (PSQLComposite.class.isAssignableFrom(field.getType())) {
                            String compStr = rs.getString(field.getName());
                            if (compStr != null) {
                                Object compInstance = PSQLCompositeHelper.parseComposite(compStr, field.getType());
                                field.set(entity, compInstance);
                            }
                        } else {
                            Object dbValue = rs.getObject(field.getName());
                            if (dbValue != null) {
                                if (field.getType().isEnum() && dbValue instanceof String) {
                                    dbValue = Enum.valueOf((Class<Enum>) field.getType(), (String) dbValue);
                                }
                                field.set(entity, dbValue);
                            }
                        }
                    }
                    results.add(entity);
                }
            }
        } catch (SQLException | ReflectiveOperationException e) {
            throw new RuntimeException("Error reading entity by value", e);
        }
        return results;
    }

    @Override
    public boolean deleteByValue(Class<?> myClass, String column, DB_Condition condition, Object value) {
        String tableName = myClass.getSimpleName();
        String query = "DELETE FROM " + tableName + " WHERE " + column + " " + condition.getOperator() + " ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, value);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting entity", e);
        }
    }

    @Override
    public void executeQuery(String query) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error executing query: " + query, e);
        }
    }

    @Override
    public List<Map<String, Object>> executeSelectQuery(String query) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (connection == null) {
            connect();
        }
        try (PreparedStatement stmt = connection.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = rs.getObject(i);
                    row.put(columnName, columnValue);
                }

                // Add the row to the result list
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing select query: " + query, e);
        }

        return rows;
    }

    @Override
    public <T> List<Map<String, Object>> joinTables(Class<T> leftTableClass, Class<T> rightTableClass, String joinColumn1, String joinColumn2) {
        List<Map<String, Object>> results = new ArrayList<>();
        String leftTableName = leftTableClass.getSimpleName();
        String rightTableName = rightTableClass.getSimpleName();

        String query = "SELECT * FROM " + leftTableName + " JOIN " + rightTableName
                + " ON " + leftTableName + "." + joinColumn1 + " = " + rightTableName + "." + joinColumn2;

        try (PreparedStatement stmt = connection.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error joining tables", e);
        }

        return results;
    }

    @Override
    public <T> List<T> readByValue(Class<T> tableClass, ConditionBuilder cdb) {
        List<T> results = new ArrayList<>();
        String tableName = tableClass.getSimpleName();
        String query = "SELECT * FROM " + tableName + " WHERE " + cdb.getCondition();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            List<Object> values = cdb.getValues();
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                Field[] fields = tableClass.getDeclaredFields();
                while (rs.next()) {
                    T entity = tableClass.getDeclaredConstructor().newInstance();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        Object dbValue = rs.getObject(field.getName());
                        if (dbValue != null) {
                            if (field.getType().isEnum() && dbValue instanceof String) {
                                dbValue = Enum.valueOf((Class<Enum>) field.getType(), (String) dbValue);
                            }
                            field.set(entity, dbValue);
                        }

                    }
                    results.add(entity);
                }
            }
        } catch (SQLException | ReflectiveOperationException e) {
            throw new RuntimeException("Error reading by condition", e);
        }

        return results;
    }

    @Override
    public <T> T updateByValue(T entity, ConditionBuilder cdb) {
        Class<?> myClass = entity.getClass();
        String tableName = myClass.getSimpleName();
        String setClause = "";
        List<Object> values = new ArrayList<>();
        Field[] fields = myClass.getDeclaredFields();

        boolean first = true;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(entity);
                if (fieldValue != null) {
                    if (!first) {
                        setClause += ", ";
                    } else {
                        first = false;
                    }
                    setClause += field.getName() + " = ?";
                    values.add(fieldValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }

        String query = "UPDATE " + tableName + " SET " + setClause + " WHERE " + cdb.getCondition() + " RETURNING *";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int idx = 1;
            for (Object val : values) {
                stmt.setObject(idx++, val);
            }
            for (Object condVal : cdb.getValues()) {
                stmt.setObject(idx++, condVal);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    for (Field field : fields) {
                        field.setAccessible(true);
                        Object dbValue = rs.getObject(field.getName());
                        if (dbValue != null) {
                            field.set(entity, dbValue);
                        }
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(PSQL_Handler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating entity", e);
        }

        return entity;
    }

    @Override
    public boolean deleteByValue(Class<?> tableClass, ConditionBuilder cdb) {
        String tableName = tableClass.getSimpleName();
        String query = "DELETE FROM " + tableName + " WHERE " + cdb.getCondition();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            List<Object> values = cdb.getValues();
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting entity", e);
        }
    }

//
//    @Override
//    public <T> List<Map<String, Object>> joinTables(Class<T> leftTableClass, Class<T> rightTableClass, String joinColumn1, String joinColumn2) {
//        List<Map<String, Object>> results = new ArrayList<>();
//        String leftTableName = leftTableClass.getSimpleName();
//        String rightTableName = rightTableClass.getSimpleName();
//
//        String query = "SELECT * FROM " + leftTableName + " JOIN " + rightTableName
//                + " ON " + leftTableName + "." + joinColumn1 + " = " + rightTableName + "." + joinColumn2;
//
//        try (PreparedStatement stmt = connection.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
//
//            ResultSetMetaData metaData = rs.getMetaData();
//            int columnCount = metaData.getColumnCount();
//
//            while (rs.next()) {
//                Map<String, Object> row = new LinkedHashMap<>();
//                for (int i = 1; i <= columnCount; i++) {
//                    String columnName = metaData.getColumnName(i);
//                    row.put(columnName, rs.getObject(i));
//                }
//                results.add(row);
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("Error joining tables", e);
//        }
//
//        return results;
//    }
}
