package com.bank.product.acceptance;

import com.bank.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProductAcceptanceTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProductRepository productRepository;

    @Test
    void 상품_단건_조회() throws Exception {
        productRepository.save("주택담보대출", new BigDecimal("3.50"), new BigDecimal("300000000"));

        mockMvc.perform(get("/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("주택담보대출"))
                .andExpect(jsonPath("$.interestRate").value(3.5));
    }

    @Test
    void 상품_목록_조회() throws Exception {
        productRepository.save("주택담보대출", new BigDecimal("3.50"), new BigDecimal("300000000"));
        productRepository.save("신용대출", new BigDecimal("4.10"), new BigDecimal("200000000"));

        mockMvc.perform(get("/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[1].productId").value(2));
    }

    @Test
    void 상품_등록_후_조회() throws Exception {
        mockMvc.perform(post("/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "productName": "신용대출",
                            "interestRate": 5.5,
                            "maxLoanAmt": 300000000
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").isNotEmpty())
                .andExpect(jsonPath("$.productName").value("신용대출"))
                .andExpect(jsonPath("$.interestRate").value(5.5));

        mockMvc.perform(get("/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("신용대출"));
    }
}
