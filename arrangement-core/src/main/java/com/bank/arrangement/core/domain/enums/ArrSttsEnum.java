package com.bank.arrangement.core.domain.enums;

public enum ArrSttsEnum {

    IN_PROGRESS("P"),
    ACTIVE("A"),
    TERMINATE("T");

    private final String code;

    ArrSttsEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}