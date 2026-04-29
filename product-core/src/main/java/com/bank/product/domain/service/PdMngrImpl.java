package com.bank.product.domain.service;

import com.bank.product.domain.repository.ProductRepository;
import com.bank.productapi.model.Pd;
import com.bank.productapi.model.PdMngr;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class PdMngrImpl implements PdMngr {

    private final ProductRepository productRepository;

    public PdMngrImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Pd getPd(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));
    }
}
