package com.bank.loanapi.enums;

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