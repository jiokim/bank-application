package com.bank.product.domain.service;

import com.bank.product.domain.repository.DpProductRepository;
import com.bank.productapi.model.DpPd;
import com.bank.productapi.model.PdMngr;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class DpPdMngrImpl implements PdMngr<DpPd> {

    private final DpProductRepository dpProductRepository;

    public DpPdMngrImpl(DpProductRepository dpProductRepository) {
        this.dpProductRepository = dpProductRepository;
    }

    @Override
    public DpPd getPd(Long productId) {
        return dpProductRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Deposit product not found: " + productId));
    }
}
