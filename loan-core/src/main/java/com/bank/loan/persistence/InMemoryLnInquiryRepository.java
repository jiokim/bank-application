package com.bank.loan.persistence;

import com.bank.loan.core.domain.model.LnInquiryCreateSpec;
import com.bank.loan.core.domain.model.LnInquiryImpl;
import com.bank.loan.domain.repository.LnInquiryRepository;
import com.bank.loan.core.domain.model.LnInquiry;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryLnInquiryRepository implements LnInquiryRepository {

    private final AtomicLong sequence = new AtomicLong(0);
    private final Map<Long, LnInquiry> inquiries = new LinkedHashMap<>();

    @Override
    public synchronized LnInquiry save(LnInquiryCreateSpec spec) {
        Long inquiryId = sequence.incrementAndGet();
        LnInquiry inquiry = new LnInquiryImpl(
                inquiryId, spec.getCustId(), LocalDate.now(), spec.getResults()
        );
        inquiries.put(inquiryId, inquiry);
        return inquiry;
    }

    @Override
    public synchronized Optional<LnInquiry> findById(Long inquiryId) {
        return Optional.ofNullable(inquiries.get(inquiryId));
    }
}