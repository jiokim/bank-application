package com.bank.product.domain.repository;

import com.bank.productapi.model.DpPd;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DpProductRepository {

    DpPd save(String productName, BigDecimal interestRate);

    Optional<DpPd> findById(Long productId);

    List<DpPd> findAll();
}
