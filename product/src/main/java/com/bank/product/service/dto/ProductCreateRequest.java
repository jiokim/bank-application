package com.bank.product.service.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductCreateRequest {

    private String productName;
    private BigDecimal interestRate;
}
