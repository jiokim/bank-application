package com.bank.product.core.autoconfigure;

import com.bank.product.core.domain.model.PdMngrImpl;
import com.bank.product.core.domain.repository.ProductRepository;
import com.bank.product.core.repository.InMemoryProductRepository;
import com.bank.productapi.model.PdMngr;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ProductAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProductRepository productRepository() {
        return new InMemoryProductRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public PdMngr pdMngr(ProductRepository productRepository) {
        return new PdMngrImpl(productRepository);
    }
}
