/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.iti.database;


public enum SQL_Condition implements DB_Condition {
    
    EQUAL("="),
    LESS("<"),
    MORE(">"),
    LESS_OR_EQUAL("<="),
    MORE_OR_EQUAL(">="),
    LIKE("LIKE"),
    IN("IN");

    private final String operator;

    SQL_Condition(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
    
}
