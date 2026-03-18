package com.ruoyi.data.clean.domain.enums;

public enum DataSourceType {
    USER("sys_user"),          // 枚举常量 USER，对应表 "sys_user"
    ROBOT_INTERACTION_LOG("robot_interaction_log");     // 枚举常量 ROBOT_INTERACTION_LOG，对应表 "robot_interaction_log"

    private final String tableName;   // 成员变量，保存表名

    // 构造方法，在创建每个枚举常量时被调用
    DataSourceType(String tableName) {
        this.tableName = tableName;
    }

    // 提供方法获取表名
    public String getTableName() {
        return tableName;
    }
}