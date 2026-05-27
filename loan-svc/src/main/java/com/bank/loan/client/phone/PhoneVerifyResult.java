package com.bank.loan.client.phone;

public record PhoneVerifyResult(boolean verified, String message) {

    public static PhoneVerifyResult success() {
        return new PhoneVerifyResult(true, "인증되었습니다.");
    }

    public static PhoneVerifyResult failure(String message) {
        return new PhoneVerifyResult(false, message);
    }
}