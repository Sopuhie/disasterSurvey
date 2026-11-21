package com.santech.framework.utils.component.cron.enums;
/*
 * type枚举
 */
public enum TypeEnum {
    DAY(1, "天/小时/分钟"), WEEK(2, "周"), MONTH(3, "月");

    int value;
    String name;

    TypeEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
