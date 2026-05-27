package com.bank.loan.client.realname;

public record RealNameVerificationResult(boolean verified, String message) {

    public static RealNameVerificationResult success() {
        return new RealNameVerificationResult(true, "실명인증 성공");
    }

    public static RealNameVerificationResult failure(String message) {
        return new RealNameVerificationResult(false, message);
    }
}
