package com.bank.product.domain.repository;

import com.bank.productapi.model.Pd;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Pd save(String productName, BigDecimal interestRate, BigDecimal maxLoanAmt, int minAge, int maxAge);

    Optional<Pd> findById(Long productId);

    List<Pd> findAll();
}
