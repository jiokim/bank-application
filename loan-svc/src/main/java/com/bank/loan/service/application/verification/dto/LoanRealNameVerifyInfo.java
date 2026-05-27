package com.bank.loan.service.application.verification.dto;

public record LoanRealNameVerifyInfo(boolean verified, Long custId, String message) {

    public static LoanRealNameVerifyInfo success(Long custId) {
        return new LoanRealNameVerifyInfo(true, custId, "실명인증 성공");
    }

    public static LoanRealNameVerifyInfo failure(String message) {
        return new LoanRealNameVerifyInfo(false, null, message);
    }
}
