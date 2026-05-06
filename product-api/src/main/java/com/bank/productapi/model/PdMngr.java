package com.bank.productapi.model;

public interface PdMngr<T extends Pd> {

    T getPd(Long productId);
}
