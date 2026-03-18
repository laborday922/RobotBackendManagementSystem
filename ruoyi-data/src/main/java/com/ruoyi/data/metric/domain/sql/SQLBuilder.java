package com.ruoyi.data.metric.domain.sql;

public class SQLBuilder {

    public static String build(String expression,String table){

        return "SELECT " + expression + " AS value FROM " + table;

    }

}