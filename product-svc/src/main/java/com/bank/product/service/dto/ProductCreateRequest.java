package com.bank.product.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductCreateRequest {

    @NotBlank
    private String productName;

    @NotNull
    private BigDecimal interestRate;

    @NotNull
    private BigDecimal maxLoanAmt;
}
