package com.bank.arrangement.core.domain.enums;

public enum ArrTpEnum {

    /** 여신 */
    LN("01"),
    /** 수신 */
    DP("02");

    private final String code;

    ArrTpEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}