package com.bank.loan.service.application.verification.dto;

import java.util.List;

public record LoanPhoneVerifyCommand(Long custId, String phoneNo, String verifyToken, String verifyCode, List<Long> agreedTermIds) {}