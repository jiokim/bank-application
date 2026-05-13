package com.bank.loan.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoanEligibilityRequest(
        @NotNull Long custId,
        @NotNull Long pdId,
        @NotBlank String lnRcvChnlCd
) {}