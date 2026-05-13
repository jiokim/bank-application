package com.bank.loan.service.application.eligibility.dto;

public record LoanEligibilityCommand(Long custId, Long pdId, String lnRcvChnlCd) {}
