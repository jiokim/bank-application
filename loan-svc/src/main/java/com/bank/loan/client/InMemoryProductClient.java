package com.bank.loan.client;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class InMemoryProductClient implements ProductClient {

    private final Map<Long, LnProductInfo> products = Map.of(
            1L, new LnProductInfo(1L, new BigDecimal("50000000"), new BigDecimal("0.045")),
            2L, new LnProductInfo(2L, new BigDecimal("30000000"), new BigDecimal("0.039"))
    );

    @Override
    public LnProductInfo getProduct(Long pdId) {
        return Optional.ofNullable(products.get(pdId))
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다: " + pdId));
    }
}
