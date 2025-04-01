package com.iti.database.mongo;



import com.iti.database.ConditionBuilder;
import com.iti.database.DB_Condition;
import com.iti.database.DB_Handler;
import com.iti.database.DB_Connection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.*;
//implements DB_Handler
public class MongoDB_Handler  {

//    private final DB_Connection dbConnection = DB_Connection.getInstance();
//
//    @Override
//    public void connect() {
//        dbConnection.connect();
//    }
//
//    @Override
//    public void disconnect() {
//        dbConnection.disconnect();
//    }
//
//    @Override
//    public boolean isConnected() {
//        return dbConnection.isConnected();
//    }
//
//    @Override
//    public <T> T create(T entity) {
//        connect();
//        MongoDatabase database = dbConnection.getMongoDatabase();
//        String collectionName = entity.getClass().getSimpleName();
//        MongoCollection<Document> collection = database.getCollection(collectionName);
//
//        Document doc = new Document();
//        for (Field field : entity.getClass().getDeclaredFields()) {
//            field.setAccessible(true);
//            try {
//                doc.append(field.getName(), field.get(entity));
//            } catch (IllegalAccessException e) {
//                throw new RuntimeException("Error accessing field " + field.getName(), e);
//            }
//        }
//
//        collection.insertOne(doc);
//        return entity;
//    }
//
//    @Override
//    public <T> T updateByValue(T entity, String whereColumn, DB_Condition condition, Object conditionValue) {
//        connect();
//        MongoDatabase database = dbConnection.getMongoDatabase();
//        String collectionName = entity.getClass().getSimpleName();
//        MongoCollection<Document> collection = database.getCollection(collectionName);
//
//        Bson filter = buildCondition(whereColumn, condition, conditionValue);
//        Document updateDoc = new Document();
//        for (Field field : entity.getClass().getDeclaredFields()) {
//            field.setAccessible(true);
//            try {
//                updateDoc.append(field.getName(), field.get(entity));
//            } catch (IllegalAccessException e) {
//                throw new RuntimeException("Error accessing field " + field.getName(), e);
//            }
//        }
//
//        collection.updateOne(filter, new Document("$set", updateDoc));
//        return entity;
//    }
//
//    @Override
//    public <T> List<T> readAll(Class<T> myClass) {
//        connect();
//        MongoDatabase database = dbConnection.getMongoDatabase();
//        String collectionName = myClass.getSimpleName();
//        MongoCollection<Document> collection = database.getCollection(collectionName);
//
//        List<T> results = new ArrayList<>();
//        for (Document doc : collection.find()) {
//            results.add(documentToEntity(doc, myClass));
//        }
//
//        return results;
//    }
//
//    @Override
//    public <T> List<T> readByValue(Class<T> tableClass, String column, DB_Condition condition, Object value) {
//        connect();
//        MongoDatabase database = dbConnection.getMongoDatabase();
//        String collectionName = tableClass.getSimpleName();
//        MongoCollection<Document> collection = database.getCollection(collectionName);
//
//        Bson filter = buildCondition(column, condition, value);
//
//        List<T> results = new ArrayList<>();
//        for (Document doc : collection.find(filter)) {
//            results.add(documentToEntity(doc, tableClass));
//        }
//
//        return results;
//    }
//
//    @Override
//    public boolean deleteByValue(Class<?> myClass, String column, DB_Condition condition, Object value) {
//        connect();
//        MongoDatabase database = dbConnection.getMongoDatabase();
//        String collectionName = myClass.getSimpleName();
//        MongoCollection<Document> collection = database.getCollection(collectionName);
//
//        Bson filter = buildCondition(column, condition, value);
//        return collection.deleteOne(filter).getDeletedCount() > 0;
//    }
//
//    @Override
//    public void executeQuery(String query) {
//        throw new UnsupportedOperationException("MongoDB does not support raw queries like SQL.");
//    }
//
//    @Override
//    public List<Map<String, Object>> executeSelectQuery(String query) {
//        throw new UnsupportedOperationException("MongoDB does not support raw queries like SQL.");
//    }
//
//    private Bson buildCondition(String column, DB_Condition condition, Object value) {
//        switch (condition) {
//            case EQUALS:
//                return eq(column, value);
//            case NOT_EQUALS:
//                return ne(column, value);
//            case GREATER_THAN:
//                return gt(column, value);
//            case LESS_THAN:
//                return lt(column, value);
//            case GREATER_THAN_OR_EQUALS:
//                return gte(column, value);
//            case LESS_THAN_OR_EQUALS:
//                return lte(column, value);
//            default:
//                throw new IllegalArgumentException("Unsupported condition: " + condition);
//        }
//    }
//
//    private <T> T documentToEntity(Document doc, Class<T> entityClass) {
//        try {
//            T entity = entityClass.getDeclaredConstructor().newInstance();
//            for (Field field : entityClass.getDeclaredFields()) {
//                field.setAccessible(true);
//                if (doc.containsKey(field.getName())) {
//                    field.set(entity, doc.get(field.getName()));
//                }
//            }
//            return entity;
//        } catch (Exception e) {
//            throw new RuntimeException("Error converting document to entity", e);
//        }
//    }
}
