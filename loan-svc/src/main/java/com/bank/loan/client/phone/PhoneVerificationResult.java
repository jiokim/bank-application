package com.bank.loan.client.phone;

public record PhoneVerificationResult(boolean sent, String verifyToken, String message) {

    public static PhoneVerificationResult success(String verifyToken) {
        return new PhoneVerificationResult(true, verifyToken, "인증번호가 발송되었습니다.");
    }

    public static PhoneVerificationResult failure(String message) {
        return new PhoneVerificationResult(false, null, message);
    }
}