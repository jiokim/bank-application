package com.bank.product.core.model;

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
    public Pd getPd(Long pdId) {
        return productRepository.findById(pdId)
            .orElseThrow(() -> new NoSuchElementException("Product not found: " + pdId));
    }
}
