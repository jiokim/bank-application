package com.bank.cust.core.autoconfigure;

import com.bank.cust.core.domain.repository.CustRepository;
import com.bank.cust.core.domain.repository.CustTermsAgreementRepository;
import com.bank.cust.core.domain.service.CustMngr;
import com.bank.cust.core.domain.service.CustMngrImpl;
import com.bank.cust.core.repository.InMemoryCustRepository;
import com.bank.cust.core.repository.InMemoryCustTermsAgreementRepository;
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

    @Bean
    @ConditionalOnMissingBean
    public CustTermsAgreementRepository custTermsAgreementRepository() {
        return new InMemoryCustTermsAgreementRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public CustMngr custMngr(CustRepository custRepository, CustTermsAgreementRepository termsAgreementRepository) {
        return new CustMngrImpl(custRepository, termsAgreementRepository);
    }
}
