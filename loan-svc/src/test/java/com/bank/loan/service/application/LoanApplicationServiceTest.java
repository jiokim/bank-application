package com.bank.loan.service.application;

import com.bank.loan.client.kcb.KcbCreditClient;
import com.bank.loan.client.kcb.KcbCreditInfo;
import com.bank.loan.client.nice.NiceCreditClient;
import com.bank.loan.client.nice.NiceCreditInfo;
import com.bank.loan.core.domain.model.LnInquiryCreateSpec;
import com.bank.loan.core.domain.model.LnInquiryImpl;
import com.bank.loan.core.domain.model.LnInquiryResultImpl;
import com.bank.loan.core.domain.repository.LnInquiryRepository;
import com.bank.loan.service.application.multistrategy.LnInquiryStrategy;
import com.bank.loan.service.dto.LoanInquiryCommand;
import com.bank.loan.service.dto.LoanInquiryInfo;
import com.bank.loanapi.model.LnInquiryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock NiceCreditClient niceClient;
    @Mock KcbCreditClient kcbClient;
    @Mock LnInquiryRepository lnInquiryRepository;
    @Mock LnInquiryStrategy creditLoanStrategy;
    @Mock LnInquiryStrategy saitdolStrategy;

    LoanApplicationService sut;

    @BeforeEach
    void setUp() {
        when(creditLoanStrategy.supportedPdId()).thenReturn(1L);
        when(saitdolStrategy.supportedPdId()).thenReturn(2L);
        sut = new LoanApplicationService(niceClient, kcbClient, lnInquiryRepository,
                List.of(creditLoanStrategy, saitdolStrategy));
    }

    @Test
    void 한도조회_요청한_상품수만큼_결과를_반환한다() {
        NiceCreditInfo nice = new NiceCreditInfo(1L, 750, 0);
        KcbCreditInfo kcb = new KcbCreditInfo(1L, 720, 0);
        LnInquiryResult creditResult = new LnInquiryResultImpl(1L, new BigDecimal("50000000"), new BigDecimal("0.045"));
        LnInquiryResult saitdolResult = new LnInquiryResultImpl(2L, new BigDecimal("20000000"), new BigDecimal("0.039"));

        when(niceClient.getNiceCredit(1L)).thenReturn(nice);
        when(kcbClient.getKcbCredit(1L)).thenReturn(kcb);
        when(creditLoanStrategy.inquire(anyLong(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(creditResult));
        when(saitdolStrategy.inquire(anyLong(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(saitdolResult));
        when(lnInquiryRepository.save(any(LnInquiryCreateSpec.class)))
                .thenAnswer(inv -> {
                    LnInquiryCreateSpec spec = inv.getArgument(0);
                    return new LnInquiryImpl(1L, spec.getCustId(), LocalDate.now(), spec.getResults());
                });

        LoanInquiryInfo info = sut.inquiry(new LoanInquiryCommand(1L, List.of(1L, 2L)));

        assertThat(info.getInquiryId()).isEqualTo(1L);
        assertThat(info.getCustId()).isEqualTo(1L);
        assertThat(info.getResults()).hasSize(2);
        assertThat(info.getResults())
                .extracting("pdId")
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void NICE_KCB는_상품수에_관계없이_각각_한번만_호출된다() {
        LnInquiryResult creditResult = new LnInquiryResultImpl(1L, new BigDecimal("50000000"), new BigDecimal("0.045"));
        LnInquiryResult saitdolResult = new LnInquiryResultImpl(2L, new BigDecimal("20000000"), new BigDecimal("0.039"));

        when(niceClient.getNiceCredit(1L)).thenReturn(new NiceCreditInfo(1L, 750, 0));
        when(kcbClient.getKcbCredit(1L)).thenReturn(new KcbCreditInfo(1L, 720, 0));
        when(creditLoanStrategy.inquire(anyLong(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(creditResult));
        when(saitdolStrategy.inquire(anyLong(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(saitdolResult));
        when(lnInquiryRepository.save(any(LnInquiryCreateSpec.class)))
                .thenAnswer(inv -> {
                    LnInquiryCreateSpec spec = inv.getArgument(0);
                    return new LnInquiryImpl(1L, spec.getCustId(), LocalDate.now(), spec.getResults());
                });

        sut.inquiry(new LoanInquiryCommand(1L, List.of(1L, 2L)));

        verify(niceClient, times(1)).getNiceCredit(1L);
        verify(kcbClient, times(1)).getKcbCredit(1L);
    }

    @Test
    void 지원하지_않는_상품_요청시_예외가_발생한다() {
        when(niceClient.getNiceCredit(1L)).thenReturn(new NiceCreditInfo(1L, 750, 0));
        when(kcbClient.getKcbCredit(1L)).thenReturn(new KcbCreditInfo(1L, 720, 0));

        assertThatThrownBy(() -> sut.inquiry(new LoanInquiryCommand(1L, List.of(99L))))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }
}
