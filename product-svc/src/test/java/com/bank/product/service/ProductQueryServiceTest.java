package com.bank.product.service;

import com.bank.product.domain.model.PdImpl;
import com.bank.product.domain.repository.ProductRepository;
import com.bank.product.service.dto.ProductResponse;
import com.bank.productapi.model.Pd;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductQueryServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductQueryService sut;

    @Test
    @DisplayName("상품 조회 시 Pd를 ProductResponse로 올바르게 매핑한다")
    void 상품_조회_매핑() {
        Pd pd = fakePd(1L, "주택담보대출", new BigDecimal("3.50"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(pd));

        ProductResponse result = sut.getProduct(1L);

        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("주택담보대출");
        assertThat(result.getInterestRate()).isEqualByComparingTo("3.50");
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 NoSuchElementException이 발생한다")
    void 존재하지_않는_상품_조회_예외() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.getProduct(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("전체 목록 조회 시 모든 상품이 ProductResponse로 변환된다")
    void 목록_조회_변환() {
        Pd pd1 = fakePd(1L, "주택담보대출", new BigDecimal("3.50"));
        Pd pd2 = fakePd(2L, "신용대출", new BigDecimal("5.50"));
        when(productRepository.findAll()).thenReturn(List.of(pd1, pd2));

        List<ProductResponse> result = sut.getProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProductId()).isEqualTo(1L);
        assertThat(result.get(1).getProductId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("상품이 없으면 빈 목록을 반환한다")
    void 목록_조회_빈_결과() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductResponse> result = sut.getProducts();

        assertThat(result).isEmpty();
    }

    private Pd fakePd(Long id, String name, BigDecimal rate) {
        return new PdImpl(id, name, rate, new BigDecimal("300000000"));
    }
}
