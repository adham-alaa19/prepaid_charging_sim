/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.iti.database.psql;

import java.lang.reflect.Field;

/**
 *
 * @author theda
 */
public class PSQLCompositeHelper {

 static Object parseComposite(String compStr, Class<?> compType) throws ReflectiveOperationException {
    compStr = compStr.trim();
    if (compStr.startsWith("ROW(")) {
        compStr = compStr.substring(4, compStr.length() - 1);
    } else if (compStr.startsWith("(")) {
        compStr = compStr.substring(1, compStr.length() - 1);
    }    
    String[] parts = compStr.split(",\\s*");
    
    Object compInstance = compType.getDeclaredConstructor().newInstance();
    Field[] compFields = compType.getDeclaredFields();
    if (parts.length != compFields.length) {
        throw new RuntimeException("Mismatch in composite field count for type: " + compType.getSimpleName());
    }
    for (int i = 0; i < compFields.length; i++) {
        compFields[i].setAccessible(true);
        Object convertedValue = convertValue(parts[i], compFields[i].getType());
        compFields[i].set(compInstance, convertedValue);
    }
    
    return compInstance;
}

static Object convertValue(String part, Class<?> targetType) {
    part = part.trim();
    if (targetType == int.class || targetType == Integer.class) {
        return Integer.valueOf(part);
    } else if (targetType == long.class || targetType == Long.class) {
        return Long.valueOf(part);
    } else if (targetType == double.class || targetType == Double.class) {
        return Double.valueOf(part);
    } else if (targetType == boolean.class || targetType == Boolean.class) {
        return Boolean.valueOf(part);
    }

    return part;
}


    
}
