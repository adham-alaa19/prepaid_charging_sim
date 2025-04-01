package com.iti.database;
import java.util.ArrayList;
import java.util.List;

public class ConditionBuilder {
    private final List<String> conditions = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();

    public ConditionBuilder where(String column, DB_Condition condition, Object value) {
        conditions.add(column + " " + condition.getOperator() + " ?");
        values.add(value);
        return this;
    }

    public ConditionBuilder and(String column, DB_Condition condition, Object value) {
        conditions.add("AND " + column + " " + condition.getOperator() + " ?");
        values.add(value);
        return this;
    }

    public ConditionBuilder or(String column, DB_Condition condition, Object value) {
        conditions.add("OR " + column + " " + condition.getOperator() + " ?");
        values.add(value);
        return this;
    }

    public ConditionBuilder openGroup() {
        conditions.add("(");
        return this;
    }

    public ConditionBuilder closeGroup() {
        conditions.add(")");
        return this;
    }

    public String getCondition() {
        return String.join(" ", conditions);
    }

    public List<Object> getValues() {
        return values;
    }
}
