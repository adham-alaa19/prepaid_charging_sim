package com.iti.database;

import java.util.List;
import java.util.Map;


public interface DB_Handler {
    
    void connect();
    void disconnect();
    boolean isConnected();
    <T> T create(T entity);
    <T> T updateByValue(T entity, String whereColumn, DB_Condition condition, Object conditionValue);
    <T> List<T> readAll(Class<T> tableClass);
    <T> List<T>   readByValue(Class<T> tableClass,String column,DB_Condition condition,Object id);
    boolean deleteByValue(Class<?> tableClass, String column,DB_Condition condition, Object value);
       
    <T> List<Map<String, Object>> joinTables(Class<T> leftTableClass, Class<T> rightTableClass, String joinColumn1, String joinColumn2);
    <T> List<T> readByValue(Class<T> tableClass, ConditionBuilder cdb);
    <T> T updateByValue(T entity, ConditionBuilder cdb);
    boolean deleteByValue(Class<?> tableClass, ConditionBuilder cdb);
  
    void executeQuery(String query);
     <T> List<Map<String, Object>> executeSelectQuery(String query);


    
    
}
