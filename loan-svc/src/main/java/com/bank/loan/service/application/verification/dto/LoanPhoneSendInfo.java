package com.bank.loan.service.application.verification.dto;

public record LoanPhoneSendInfo(boolean sent, String verifyToken, String phoneNo, String message) {

    public static LoanPhoneSendInfo success(String verifyToken, String phoneNo) {
        return new LoanPhoneSendInfo(true, verifyToken, phoneNo, "인증번호가 발송되었습니다.");
    }

    public static LoanPhoneSendInfo failure(String message) {
        return new LoanPhoneSendInfo(false, null, null, message);
    }
}