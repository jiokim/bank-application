package com.bank.product.service;

import com.bank.product.service.dto.ProductResponse;
import com.bank.productapi.model.Pd;
import com.bank.productapi.model.PdMngr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    public final PdMngr pdMngr;

    public ProductResponse getProduct(Long productId) {
        Pd pd = pdMngr.getPd(productId);
        return new ProductResponse(pd.getPdId(), pd.getPdNm(), pd.getInterestRate());
    }

    public List<ProductResponse> getProducts() {
        return Collections.emptyList();
    }
}
