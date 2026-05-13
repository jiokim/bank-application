package com.bank.product.repository;

import com.bank.product.domain.model.LnPdImpl;
import com.bank.product.domain.repository.ProductRepository;
import com.bank.productapi.model.Pd;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryProductRepository implements ProductRepository {

    private final AtomicLong sequence = new AtomicLong(0);
    private final Map<Long, Pd> products = new LinkedHashMap<>();

    @Override
    public synchronized Pd save(String productName, BigDecimal interestRate, BigDecimal maxLoanAmt,
                                int minAge, int maxAge) {
        Long productId = sequence.incrementAndGet();
        Pd product = new LnPdImpl(productId, productName, interestRate, maxLoanAmt, minAge, maxAge);
        products.put(productId, product);
        return product;
    }

    @Override
    public synchronized Optional<Pd> findById(Long productId) {
        return Optional.ofNullable(products.get(productId));
    }

    @Override
    public synchronized List<Pd> findAll() {
        return new ArrayList<>(products.values());
    }
}
