package com.bank.product.domain.service;

import com.bank.product.domain.repository.ProductRepository;
import com.bank.productapi.model.LnPd;
import com.bank.productapi.model.PdMngr;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class LnPdMngrImpl implements PdMngr<LnPd> {

    private final ProductRepository productRepository;

    public LnPdMngrImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public LnPd getPd(Long productId) {
        return (LnPd) productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Loan product not found: " + productId));
    }
}
