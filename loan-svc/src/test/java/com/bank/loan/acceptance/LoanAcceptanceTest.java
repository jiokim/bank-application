package com.bank.loan.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LoanAcceptanceTest {

    @Autowired
    WebApplicationContext wac;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void 멀티_한도조회_3개_상품_결과_반환() throws Exception {
        mockMvc.perform(post("/v1/loans/inquiry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "custId": 1,
                                    "pdIds": ["CREDIT_LOAN", "SAITDOL", "HAETSALLON"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inquiryId").isNotEmpty())
                .andExpect(jsonPath("$.custId").value(1))
                .andExpect(jsonPath("$.results.length()").value(3))
                .andExpect(jsonPath("$.results[?(@.pdId == 'CREDIT_LOAN')].maxLoanAmt").value(50000000))
                .andExpect(jsonPath("$.results[?(@.pdId == 'SAITDOL')].maxLoanAmt").value(20000000))
                .andExpect(jsonPath("$.results[?(@.pdId == 'HAETSALLON')].maxLoanAmt").value(15000000));
    }
}
