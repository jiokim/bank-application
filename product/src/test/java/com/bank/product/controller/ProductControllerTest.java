package com.bank.product.controller;

import com.bank.product.service.ProductCommandService;
import com.bank.product.service.ProductQueryService;
import com.bank.product.service.dto.ProductCreateRequest;
import com.bank.product.service.dto.ProductCreateResponse;
import com.bank.product.service.dto.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductQueryService productQueryService;

    @MockitoBean
    ProductCommandService productCommandService;

    @Test
    @DisplayName("상품 ID로 단건 조회 시 200과 상품 정보를 반환한다")
    void 상품_단건_조회() throws Exception {
        when(productQueryService.getProduct(1L))
                .thenReturn(new ProductResponse(1L, "주택담보대출", new BigDecimal("3.50")));

        mockMvc.perform(get("/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("주택담보대출"))
                .andExpect(jsonPath("$.interestRate").value(3.5));
    }

    @Test
    @DisplayName("전체 상품 목록 조회 시 200과 상품 목록을 반환한다")
    void 상품_목록_조회() throws Exception {
        when(productQueryService.getProducts())
                .thenReturn(List.of(
                        new ProductResponse(1L, "주택담보대출", new BigDecimal("3.50")),
                        new ProductResponse(2L, "신용대출", new BigDecimal("4.10"))
                ));

        mockMvc.perform(get("/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].productName").value("주택담보대출"))
                .andExpect(jsonPath("$[1].productId").value(2))
                .andExpect(jsonPath("$[1].interestRate").value(4.1));
    }

    @Test
    @DisplayName("상품 등록 요청 시 200과 등록된 상품 정보를 반환한다")
    void 상품_등록() throws Exception {
        when(productCommandService.create(argThat(this::sameCreateRequest)))
                .thenReturn(new ProductCreateResponse(10L, "신용대출", new BigDecimal("5.50")));

        mockMvc.perform(post("/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productName": "신용대출",
                                  "interestRate": 5.5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.productName").value("신용대출"))
                .andExpect(jsonPath("$.interestRate").value(5.5));
    }

    @Test
    @DisplayName("상품명 없이 등록 요청 시 400을 반환한다")
    void 상품명_없이_등록_시_400() throws Exception {
        mockMvc.perform(post("/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "interestRate": 5.5
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private boolean sameCreateRequest(ProductCreateRequest request) {
        return request != null
                && "신용대출".equals(request.getProductName())
                && new BigDecimal("5.5").compareTo(request.getInterestRate()) == 0;
    }
}
