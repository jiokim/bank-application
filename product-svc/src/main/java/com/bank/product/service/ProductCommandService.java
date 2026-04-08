package com.bank.product.service;

import com.bank.product.core.domain.repository.ProductRepository;
import com.bank.product.service.dto.ProductCreateRequest;
import com.bank.product.service.dto.ProductCreateResponse;
import com.bank.productapi.model.Pd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

    private final ProductRepository productRepository;

    public ProductCreateResponse create(ProductCreateRequest productCreateRequest) {
        Pd product = productRepository.save(
                productCreateRequest.getProductName(),
                productCreateRequest.getInterestRate(),
                productCreateRequest.getMaxLoanAmt()
        );

        return new ProductCreateResponse(
                product.getPdId(),
                product.getPdNm(),
                product.getInterestRate(),
                product.getMaxLoanAmt()
        );
    }
}