package com.bank.product.service.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductCreateResponse {

    private Long productId;
    private String productName;
    private BigDecimal interestRate;

    public ProductCreateResponse() {
    }

    public ProductCreateResponse(Long productId, String productName, BigDecimal interestRate) {
        this.productId = productId;
        this.productName = productName;
        this.interestRate = interestRate;
    }
}
