package com.bank.loan.service.application.verification.dto;

public record LoanPhoneVerifyInfo(boolean verified, String message) {

    public static LoanPhoneVerifyInfo success() {
        return new LoanPhoneVerifyInfo(true, "인증되었습니다.");
    }

    public static LoanPhoneVerifyInfo failure(String message) {
        return new LoanPhoneVerifyInfo(false, message);
    }
}