package com.bank.loan.controller.dto;

public record LoanEligibilityResponse(
        String aplctnAblYn,
        String lmtQryNbr,
        String rsltCd,
        String rsltMsgCntnt
) {}