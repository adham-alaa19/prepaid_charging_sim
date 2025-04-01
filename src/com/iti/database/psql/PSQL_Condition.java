/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.iti.database.psql;

import com.iti.database.DB_Condition;

/**
 *
 * @author theda
 */
public enum PSQL_Condition implements DB_Condition {
    
    ILIKE("ILIKE");
    private final String operator;

    PSQL_Condition(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
    
}
