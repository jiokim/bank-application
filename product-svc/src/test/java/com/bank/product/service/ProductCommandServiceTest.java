package com.bank.product.service;

import com.bank.product.core.domain.model.PdImpl;
import com.bank.product.core.domain.repository.ProductRepository;
import com.bank.product.service.dto.ProductCreateRequest;
import com.bank.product.service.dto.ProductCreateResponse;
import com.bank.productapi.model.Pd;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductCommandService sut;

    private static final BigDecimal MAX_LOAN_AMT = new BigDecimal("300000000");

    @Test
    @DisplayName("요청의 상품명과 금리가 Repository에 올바르게 전달된다")
    void 등록_요청값_전달() {
        ProductCreateRequest request = request("신용대출", new BigDecimal("5.50"), MAX_LOAN_AMT);
        Pd saved = fakePd(1L, "신용대출", new BigDecimal("5.50"), MAX_LOAN_AMT);
        when(productRepository.save("신용대출", new BigDecimal("5.50"), MAX_LOAN_AMT)).thenReturn(saved);

        sut.create(request);

        verify(productRepository).save("신용대출", new BigDecimal("5.50"), MAX_LOAN_AMT);
    }

    @Test
    @DisplayName("저장된 Pd를 ProductCreateResponse로 올바르게 매핑한다")
    void 등록_결과_매핑() {
        ProductCreateRequest request = request("신용대출", new BigDecimal("5.50"), MAX_LOAN_AMT);
        Pd saved = fakePd(10L, "신용대출", new BigDecimal("5.50"), MAX_LOAN_AMT);
        when(productRepository.save("신용대출", new BigDecimal("5.50"), MAX_LOAN_AMT)).thenReturn(saved);

        ProductCreateResponse result = sut.create(request);

        assertThat(result.getProductId()).isEqualTo(10L);
        assertThat(result.getProductName()).isEqualTo("신용대출");
        assertThat(result.getInterestRate()).isEqualByComparingTo("5.50");
        assertThat(result.getMaxLoanAmt()).isEqualByComparingTo("300000000");
    }

    private ProductCreateRequest request(String name, BigDecimal rate, BigDecimal maxLoanAmt) {
        ProductCreateRequest request = mock(ProductCreateRequest.class);
        when(request.getProductName()).thenReturn(name);
        when(request.getInterestRate()).thenReturn(rate);
        when(request.getMaxLoanAmt()).thenReturn(maxLoanAmt);
        return request;
    }

    private Pd fakePd(Long id, String name, BigDecimal rate, BigDecimal maxLoanAmt) {
        return new PdImpl(id, name, rate, maxLoanAmt);
    }
}
