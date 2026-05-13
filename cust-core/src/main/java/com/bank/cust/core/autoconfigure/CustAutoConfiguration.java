package com.bank.cust.core.autoconfigure;

import com.bank.cust.core.domain.repository.CustRepository;
import com.bank.cust.core.repository.InMemoryCustRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CustAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CustRepository custRepository() {
        return new InMemoryCustRepository();
    }
}