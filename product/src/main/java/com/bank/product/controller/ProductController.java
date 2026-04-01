package com.bank.product.controller;

import com.bank.product.service.ProductCommandService;
import com.bank.product.service.ProductQueryService;
import com.bank.product.service.dto.ProductCreateRequest;
import com.bank.product.service.dto.ProductCreateResponse;
import com.bank.product.service.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductQueryService productQueryService;
    private final ProductCommandService productCommandService;

    @GetMapping("/v1/products/{productId}")
    public ProductResponse getProduct(@PathVariable Long productId) {
        return productQueryService.getProduct(productId);
    }

    @GetMapping("/v1/products")
    public List<ProductResponse> getProducts() {
        return productQueryService.getProducts();
    }

    @PostMapping("/v1/products")
    public ProductCreateResponse createProduct(@RequestBody ProductCreateRequest request) {
        return productCommandService.create(request);
    }
}
