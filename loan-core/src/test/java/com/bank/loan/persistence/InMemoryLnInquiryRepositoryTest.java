package com.bank.loan.persistence;

import com.bank.loan.core.domain.model.LnInquiry;
import com.bank.loan.core.domain.model.LnInquiryCreateSpec;
import com.bank.loan.core.domain.model.LnInquiryResult;
import com.bank.loan.core.domain.model.LnInquiryResultImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryLnInquiryRepositoryTest {

    private final InMemoryLnInquiryRepository repository = new InMemoryLnInquiryRepository();

    @Test
    void saveDoesNotKeepMutableResultsReference() {
        List<LnInquiryResult> results = new ArrayList<>();
        results.add(result(1L));

        LnInquiry inquiry = repository.save(new LnInquiryCreateSpec(1L, results));

        results.add(result(2L));

        assertThat(inquiry.getResults())
                .extracting(LnInquiryResult::getPdId)
                .containsExactly(1L);
        assertThat(repository.findById(inquiry.getInquiryId()).orElseThrow().getResults())
                .extracting(LnInquiryResult::getPdId)
                .containsExactly(1L);
    }

    @Test
    void savedInquiryResultsAreReadOnly() {
        LnInquiry inquiry = repository.save(new LnInquiryCreateSpec(1L, List.of(result(1L))));

        assertThatThrownBy(() -> inquiry.getResults().add(result(2L)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private LnInquiryResult result(Long pdId) {
        return new LnInquiryResultImpl(pdId, new BigDecimal("10000000"), new BigDecimal("0.05"));
    }
}