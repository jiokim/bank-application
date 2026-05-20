package com.bank.loan.controller;

import com.bank.loan.controller.dto.LoanRealNameVerificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = LoanCustVerifyController.class)
@DisplayName("대출신청 실명인증 API")
class LoanCustVerifyControllerTest {

    @Autowired
    MockMvcTester mockMvcTester;

    @DisplayName("[성공] 실명인증이 정상처리되면 custId를 반환한다")
    @Test
    void 실명인증_요청이_정상이면_200을_반환한다() {
        MvcTestResult result = mockMvcTester.post()
                .uri("/v1/loans/real-name-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "custNm": "홍길동",
                          "rnmNbr": "8901011000001"
                        }
                        """)
                .exchange();

        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .convertTo(LoanRealNameVerificationResponse.class)
                .satisfies(response -> {
                    assertThat(response.realNameVerified()).isTrue();
                    assertThat(response.custId()).isNotNull();
                });
    }

    @DisplayName("[실패] 고객명이 없으면 400을 반환한다")
    @Test
    void 고객명이_없으면_400을_반환한다() {
        MvcTestResult result = mockMvcTester.post()
                .uri("/v1/loans/real-name-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "custNm": null,
                          "rnmNbr": "8901011000001"
                        }
                        """)
                .exchange();

        assertThat(result).hasStatus4xxClientError();
    }

    @DisplayName("[실패] 실명번호가 없으면 400을 반환한다")
    @Test
    void 실명번호가_없으면_400을_반환한다() {
        MvcTestResult result = mockMvcTester.post()
                .uri("/v1/loans/real-name-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "custNm": "홍길동",
                          "rnmNbr": null
                        }
                        """)
                .exchange();

        assertThat(result).hasStatus4xxClientError();
    }
}
