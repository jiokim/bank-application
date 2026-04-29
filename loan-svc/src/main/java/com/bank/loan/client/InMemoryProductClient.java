package com.bank.loan.client;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class InMemoryProductClient implements ProductClient {

    private final Map<String, LnProductInfo> products = Map.of(
            "PD1", new LnProductInfo("PD1", new BigDecimal("50000000"), new BigDecimal("0.045")),
            "PD2", new LnProductInfo("PD2", new BigDecimal("30000000"), new BigDecimal("0.039"))
    );

    @Override
    public LnProductInfo getProduct(String pdId) {
        return Optional.ofNullable(products.get(pdId))
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다: " + pdId));
    }
}