package com.bank.loan.core.domain.enums;

public enum ArrSttsEnum {

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