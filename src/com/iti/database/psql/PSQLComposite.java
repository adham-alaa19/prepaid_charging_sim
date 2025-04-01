/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.iti.database.psql;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author theda
 */
public interface PSQLComposite {
    default String toRow() {
        StringBuilder rowBuilder = new StringBuilder("ROW(");
        Field[] fields = this.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true); 
            try {
                Object value = fields[i].get(this);
                if (value instanceof String) {
                    rowBuilder.append("'").append(value).append("'");
                } else {
                    rowBuilder.append(value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (i < fields.length - 1) {
                rowBuilder.append(", ");
            }
        }

        rowBuilder.append(")");
        return rowBuilder.toString();
    }
    
        
    default String toRowPlaceHolder() {
        StringBuilder placeholderBuilder = new StringBuilder("ROW(");
        Field[] fields = this.getClass().getDeclaredFields();
        
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                placeholderBuilder.append(", ");
            }
            placeholderBuilder.append("?");
        }
        
        placeholderBuilder.append(")");
        return placeholderBuilder.toString();
    }
    
    default Map<String, Object> getValues() {
        Map<String, Object> valuesMap = new LinkedHashMap<>();;
        Field[] fields = this.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                valuesMap.put(field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        
        return valuesMap;
    }
}  
    
    

