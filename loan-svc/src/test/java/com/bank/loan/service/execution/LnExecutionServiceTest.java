package com.bank.loan.service.execution;

import com.bank.loan.core.domain.model.LnArrCreateSpec;
import com.bank.loan.core.domain.model.LnArrImpl;
import com.bank.loan.core.domain.model.LnInquiryImpl;
import com.bank.loan.core.domain.model.LnInquiryResultImpl;
import com.bank.loan.core.domain.repository.LnInquiryRepository;
import com.bank.loan.core.domain.repository.LoanRepository;
import com.bank.loan.service.execution.dto.LoanApplyCommand;
import com.bank.loan.service.execution.dto.LoanApplyInfo;
import com.bank.arrangement.core.domain.enums.ArrSttsEnum;
import com.bank.loan.core.domain.model.LnInquiry;
import com.bank.loan.core.domain.model.LnInquiryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentCaptor.forClass;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LnExecutionServiceTest {

    @Mock LnInquiryRepository lnInquiryRepository;
    @Mock LoanRepository loanRepository;

    LnExecutionService sut;

    @BeforeEach
    void setUp() {
        sut = new LnExecutionService(lnInquiryRepository, loanRepository);
    }

    @Test
    void 대출신청_성공() {
        LnInquiry inquiry = inquiry(LocalDate.now(), List.of(
                new LnInquiryResultImpl(1L, new BigDecimal("50000000"), new BigDecimal("0.045"))
        ));
        when(lnInquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));
        when(loanRepository.save(any(LnArrCreateSpec.class))).thenAnswer(inv -> {
            LnArrCreateSpec spec = inv.getArgument(0);
            return new LnArrImpl(100L, spec.getCustId(), spec.getPdId(),
                    spec.getLnAmt(), spec.getIntrRt(),
                    ArrSttsEnum.ACTIVE, spec.getArrStrtDt(), spec.getArrEndDt());
        });

        LoanApplyInfo info = sut.apply(new LoanApplyCommand(1L, 1L, new BigDecimal("30000000"), "ENC(rrn)"));

        assertThat(info.getArrId()).isEqualTo(100L);
        assertThat(info.getPdId()).isEqualTo(1L);
        assertThat(info.getLnAmt()).isEqualByComparingTo("30000000");
        assertThat(info.getArrSttsCd()).isEqualTo(ArrSttsEnum.ACTIVE);
    }

    @Test
    void 대출신청시_암호화된_주민등록번호를_계약생성_spec으로_전달한다() {
        LnInquiry inquiry = inquiry(LocalDate.now(), List.of(
                new LnInquiryResultImpl(1L, new BigDecimal("50000000"), new BigDecimal("0.045"))
        ));
        ArgumentCaptor<LnArrCreateSpec> captor = forClass(LnArrCreateSpec.class);
        when(lnInquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));
        when(loanRepository.save(captor.capture())).thenAnswer(inv -> {
            LnArrCreateSpec spec = inv.getArgument(0);
            return new LnArrImpl(100L, spec.getCustId(), spec.getPdId(),
                    spec.getLnAmt(), spec.getIntrRt(),
                    ArrSttsEnum.ACTIVE, spec.getArrStrtDt(), spec.getArrEndDt());
        });

        sut.apply(new LoanApplyCommand(1L, 1L, new BigDecimal("30000000"), "ENC(9001011000000)"));

        assertThat(captor.getValue().getCustRrn()).isEqualTo("ENC(9001011000000)");
    }

    @Test
    void 존재하지_않는_inquiryId_예외() {
        when(lnInquiryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.apply(new LoanApplyCommand(99L, 1L, new BigDecimal("10000000"), "ENC(rrn)")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("한도조회 이력이 없습니다");
    }

    @Test
    void 만료된_한도조회_예외() {
        LnInquiry inquiry = inquiry(LocalDate.now().minusDays(1), List.of(
                new LnInquiryResultImpl(1L, new BigDecimal("50000000"), new BigDecimal("0.045"))
        ));
        when(lnInquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        assertThatThrownBy(() -> sut.apply(new LoanApplyCommand(1L, 1L, new BigDecimal("10000000"), "ENC(rrn)")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("만료");
    }

    @Test
    void 조회하지_않은_상품_예외() {
        LnInquiry inquiry = inquiry(LocalDate.now(), List.of(
                new LnInquiryResultImpl(1L, new BigDecimal("50000000"), new BigDecimal("0.045"))
        ));
        when(lnInquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        assertThatThrownBy(() -> sut.apply(new LoanApplyCommand(1L, 2L, new BigDecimal("10000000"), "ENC(rrn)")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2");
    }

    @Test
    void 신청금액이_한도_초과시_예외() {
        LnInquiry inquiry = inquiry(LocalDate.now(), List.of(
                new LnInquiryResultImpl(1L, new BigDecimal("50000000"), new BigDecimal("0.045"))
        ));
        when(lnInquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        assertThatThrownBy(() -> sut.apply(new LoanApplyCommand(1L, 1L, new BigDecimal("60000000"), "ENC(rrn)")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("한도를 초과");
    }

    private LnInquiry inquiry(LocalDate inquiryDt, List<LnInquiryResult> results) {
        return new LnInquiryImpl(1L, 1L, inquiryDt, results);
    }
}
