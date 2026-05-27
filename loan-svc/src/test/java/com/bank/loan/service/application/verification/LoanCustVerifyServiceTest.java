package com.bank.loan.service.application.verification;

import com.bank.cust.core.domain.model.Cust;
import com.bank.cust.core.domain.service.CustMngr;
import com.bank.loan.client.realname.RealNameVerificationClient;
import com.bank.loan.client.realname.RealNameVerificationResult;
import com.bank.loan.service.application.verification.dto.LoanRealNameVerifyCommand;
import com.bank.loan.service.application.verification.dto.LoanRealNameVerifyInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("실명인증 서비스")
class LoanCustVerifyServiceTest {

    @Mock
    RealNameVerificationClient realNameVerificationClient;
    @Mock
    CustMngr custMngr;

    LoanCustVerifyService sut;

    @BeforeEach
    void setUp() {
        sut = new LoanCustVerifyService(realNameVerificationClient, custMngr);
    }

    @DisplayName("[성공] 기존 고객이면 custId를 반환하고 고객 생성을 하지 않는다")
    @Test
    void 실명인증_성공_시_기존_고객이면_custId를_반환한다() {
        // given
        Cust existingCust = mock(Cust.class);
        when(existingCust.getCustId()).thenReturn(1L);
        when(realNameVerificationClient.verify(anyString(), anyString()))
                .thenReturn(RealNameVerificationResult.success());
        when(custMngr.findCustByRnmNbr("8901011000001"))
                .thenReturn(Optional.of(existingCust));

        // when
        LoanRealNameVerifyInfo info = sut.verifyRealName(new LoanRealNameVerifyCommand("홍길동", "8901011000001"));

        // then
        assertThat(info.verified()).isTrue();
        assertThat(info.custId()).isEqualTo(1L);
        verify(custMngr, never()).create(anyString(), anyString());
    }

    @DisplayName("[성공] 신규 고객이면 고객을 생성하고 custId를 반환한다")
    @Test
    void 실명인증_성공_시_신규_고객이면_고객을_생성한다() {
        // given
        Cust newCust = mock(Cust.class);
        when(newCust.getCustId()).thenReturn(2L);
        when(realNameVerificationClient.verify(anyString(), anyString()))
                .thenReturn(RealNameVerificationResult.success());
        when(custMngr.findCustByRnmNbr("8901011000001"))
                .thenReturn(Optional.empty());
        when(custMngr.create("홍길동", "8901011000001"))
                .thenReturn(newCust);

        // when
        LoanRealNameVerifyInfo info = sut.verifyRealName(new LoanRealNameVerifyCommand("홍길동", "8901011000001"));

        // then
        assertThat(info.verified()).isTrue();
        assertThat(info.custId()).isEqualTo(2L);
        verify(custMngr).create("홍길동", "8901011000001");
    }

    @DisplayName("[실패] 실명인증 실패 시 verified=false와 실패 메시지를 반환한다")
    @Test
    void 실명인증_실패_시_verified_false를_반환한다() {
        // given
        when(realNameVerificationClient.verify(anyString(), anyString()))
                .thenReturn(RealNameVerificationResult.failure("실명 불일치"));

        // when
        LoanRealNameVerifyInfo info = sut.verifyRealName(new LoanRealNameVerifyCommand("홍길동", "8901011000001"));

        // then
        assertThat(info.verified()).isFalse();
        assertThat(info.message()).isEqualTo("실명 불일치");
        verify(custMngr, never()).findCustByRnmNbr(anyString());
    }

    @DisplayName("[실패] 고객명이 없으면 IllegalArgumentException이 발생한다")
    @Test
    void 고객명이_없으면_예외가_발생한다() {
        // given
        LoanRealNameVerifyCommand command = new LoanRealNameVerifyCommand("", "8901011000001");

        // when & then
        assertThatThrownBy(() -> sut.verifyRealName(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("고객명");
    }

    @DisplayName("[실패] 실명번호가 없으면 IllegalArgumentException이 발생한다")
    @Test
    void 실명번호가_없으면_예외가_발생한다() {
        // given
        LoanRealNameVerifyCommand command = new LoanRealNameVerifyCommand("홍길동", "");

        // when & then
        assertThatThrownBy(() -> sut.verifyRealName(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("실명번호");
    }
}
