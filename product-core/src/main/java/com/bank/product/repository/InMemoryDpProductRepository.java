package com.bank.product.repository;

import com.bank.product.domain.model.DpPdImpl;
import com.bank.product.domain.repository.DpProductRepository;
import com.bank.productapi.model.DpPd;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryDpProductRepository implements DpProductRepository {

    private final AtomicLong sequence = new AtomicLong(0);
    private final Map<Long, DpPd> products = new LinkedHashMap<>();

    @Override
    public synchronized DpPd save(String productName, BigDecimal interestRate) {
        Long productId = sequence.incrementAndGet();
        DpPd product = new DpPdImpl(productId, productName, interestRate);
        products.put(productId, product);
        return product;
    }

    @Override
    public synchronized Optional<DpPd> findById(Long productId) {
        return Optional.ofNullable(products.get(productId));
    }

    @Override
    public synchronized List<DpPd> findAll() {
        return new ArrayList<>(products.values());
    }
}
