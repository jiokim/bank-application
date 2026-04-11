package com.bank.loan.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LoanApplyRequest {

    @NotNull
    private Long custId;

    @NotNull
    private Long pdId;

    @NotNull
    @Positive
    private BigDecimal lnAmt;
}
