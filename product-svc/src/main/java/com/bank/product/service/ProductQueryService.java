package com.bank.product.service;

import com.bank.product.domain.repository.ProductRepository;
import com.bank.product.service.dto.ProductResponse;
import com.bank.productapi.model.Pd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;

    public ProductResponse getProduct(Long productId) {
        Pd pd = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));
        return new ProductResponse(pd.getPdId(), pd.getPdNm(), pd.getInterestRate(), pd.getMaxLoanAmt());
    }

    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(pd -> new ProductResponse(pd.getPdId(), pd.getPdNm(), pd.getInterestRate(), pd.getMaxLoanAmt()))
                .toList();
    }
}
