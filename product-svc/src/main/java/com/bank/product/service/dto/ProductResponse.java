package com.bank.product.service.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductResponse {

    private Long productId;
    private String productName;
    private BigDecimal interestRate;
    private BigDecimal maxLoanAmt;

    public ProductResponse() {
    }

    public ProductResponse(Long productId, String productName, BigDecimal interestRate, BigDecimal maxLoanAmt) {
        this.productId = productId;
        this.productName = productName;
        this.interestRate = interestRate;
        this.maxLoanAmt = maxLoanAmt;
    }
}
