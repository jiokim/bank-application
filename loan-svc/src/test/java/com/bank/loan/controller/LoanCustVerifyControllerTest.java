package com.bank.loan.controller;

import com.bank.loan.controller.dto.LoanPhoneSendResponse;
import com.bank.loan.controller.dto.LoanRealNameVerificationResponse;
import com.bank.loan.service.application.verification.LoanCustVerifyService;
import com.bank.loan.service.application.verification.dto.LoanPhoneSendInfo;
import com.bank.loan.service.application.verification.dto.LoanRealNameVerifyInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = LoanCustVerifyController.class)
@DisplayName("대출신청 본인인증 API")
class LoanCustVerifyControllerTest {

    @Autowired
    MockMvcTester mockMvcTester;

    @MockitoBean
    LoanCustVerifyService loanCustVerifyService;

    @Nested
    @DisplayName("실명인증")
    class VerifyRealName {

        @DisplayName("[성공] 실명인증이 정상처리되면 custId를 반환한다")
        @Test
        void 실명인증_요청이_정상이면_200을_반환한다() {
            given(loanCustVerifyService.verifyRealName(any()))
                    .willReturn(LoanRealNameVerifyInfo.success(1L));

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
                        assertThat(response.custId()).isEqualTo(1L);
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

        @DisplayName("[실패] 실명인증이 실패하면 realNameVerified=false를 반환한다")
        @Test
        void 실명인증_실패_시_realNameVerified_false를_반환한다() {
            given(loanCustVerifyService.verifyRealName(any()))
                    .willReturn(LoanRealNameVerifyInfo.failure("실명 불일치"));

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
                        assertThat(response.realNameVerified()).isFalse();
                        assertThat(response.custId()).isNull();
                    });
        }

    } // end VerifyRealName

    @Nested
    @DisplayName("휴대폰 인증번호 발송")
    class SendPhone {

        @DisplayName("[성공] 인증번호 발송이 정상처리되면 verifyToken을 반환한다")
        @Test
        void 휴대폰_인증번호_발송_정상이면_200을_반환한다() {
            given(loanCustVerifyService.sendPhone(any()))
                    .willReturn(LoanPhoneSendInfo.success("test-token", "01012345678"));

            MvcTestResult result = mockMvcTester.post()
                    .uri("/v1/loans/phone-verification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "custId": 1,
                              "phoneNo": "01012345678"
                            }
                            """)
                    .exchange();

            assertThat(result)
                    .hasStatusOk()
                    .bodyJson()
                    .convertTo(LoanPhoneSendResponse.class)
                    .satisfies(response -> {
                        assertThat(response.sent()).isTrue();
                        assertThat(response.verifyToken()).isEqualTo("test-token");
                        assertThat(response.phoneNo()).isEqualTo("01012345678");
                    });
        }

        @DisplayName("[실패] 고객 ID가 없으면 400을 반환한다")
        @Test
        void 고객ID가_없으면_400을_반환한다() {
            MvcTestResult result = mockMvcTester.post()
                    .uri("/v1/loans/phone-verification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "custId": null,
                              "phoneNo": "01012345678"
                            }
                            """)
                    .exchange();

            assertThat(result).hasStatus4xxClientError();
        }

        @DisplayName("[실패] 휴대폰 번호가 없으면 400을 반환한다")
        @Test
        void 휴대폰번호가_없으면_400을_반환한다() {
            MvcTestResult result = mockMvcTester.post()
                    .uri("/v1/loans/phone-verification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "custId": 1,
                              "phoneNo": null
                            }
                            """)
                    .exchange();

            assertThat(result).hasStatus4xxClientError();
        }

        @DisplayName("[실패] 미가입 고객이면 sent=false를 반환한다")
        @Test
        void 미가입_고객이면_sent_false를_반환한다() {
            given(loanCustVerifyService.sendPhone(any()))
                    .willReturn(LoanPhoneSendInfo.failure("미가입 고객입니다."));

            MvcTestResult result = mockMvcTester.post()
                    .uri("/v1/loans/phone-verification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "custId": 99,
                              "phoneNo": "01012345678"
                            }
                            """)
                    .exchange();

            assertThat(result)
                    .hasStatusOk()
                    .bodyJson()
                    .convertTo(LoanPhoneSendResponse.class)
                    .satisfies(response -> {
                        assertThat(response.sent()).isFalse();
                        assertThat(response.verifyToken()).isNull();
                    });
        }

        @DisplayName("[실패] 인증번호 발송이 실패하면 sent=false를 반환한다")
        @Test
        void 인증번호_발송_실패_시_sent_false를_반환한다() {
            given(loanCustVerifyService.sendPhone(any()))
                    .willReturn(LoanPhoneSendInfo.failure("통신사 서비스 오류"));

            MvcTestResult result = mockMvcTester.post()
                    .uri("/v1/loans/phone-verification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "custId": 1,
                              "phoneNo": "01012345678"
                            }
                            """)
                    .exchange();

            assertThat(result)
                    .hasStatusOk()
                    .bodyJson()
                    .convertTo(LoanPhoneSendResponse.class)
                    .satisfies(response -> {
                        assertThat(response.sent()).isFalse();
                        assertThat(response.verifyToken()).isNull();
                    });
        }
    }
}
