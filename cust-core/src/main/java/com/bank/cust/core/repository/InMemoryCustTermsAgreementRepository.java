package com.bank.cust.core.repository;

import com.bank.cust.core.domain.model.CustTermsAgreement;
import com.bank.cust.core.domain.repository.CustTermsAgreementRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryCustTermsAgreementRepository implements CustTermsAgreementRepository {

    private final AtomicLong sequence = new AtomicLong(0L);
    private final Map<Long, CustTermsAgreement> store = new ConcurrentHashMap<>();

    @Override
    public CustTermsAgreement save(Long custId, List<Long> agreedTermIds) {
        Long id = sequence.incrementAndGet();
        CustTermsAgreement agreement = new CustTermsAgreement(id, custId, List.copyOf(agreedTermIds), LocalDateTime.now());
        store.put(id, agreement);
        return agreement;
    }
}