package com.bank.loan.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LoanApplyRequest {

    @NotNull
    private Long inquiryId;

    @NotNull
    private String pdId;

    @NotNull
    @Positive
    private BigDecimal lnAmt;
}
