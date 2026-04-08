package com.bank.product.acceptance;

import com.bank.product.core.domain.repository.ProductRepository;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProductAcceptanceTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @Test
    void 상품_단건_조회() {
        productRepository.save("주택담보대출", new BigDecimal("3.50"));

        given()
            .when()
                .get("/v1/products/1")
            .then()
                .statusCode(200)
                .body("productId", equalTo(1))
                .body("productName", equalTo("주택담보대출"))
                .body("interestRate", equalTo(3.5f));
    }

    @Test
    void 상품_목록_조회() {
        productRepository.save("주택담보대출", new BigDecimal("3.50"));
        productRepository.save("신용대출", new BigDecimal("4.10"));

        given()
            .when()
                .get("/v1/products")
            .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].productId", equalTo(1))
                .body("[1].productId", equalTo(2));
    }

    @Test
    void 상품_등록_후_조회() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("""
                    {
                        "productName": "신용대출",
                        "interestRate": 5.5
                    }
                    """)
            .when()
                .post("/v1/products")
            .then()
                .statusCode(200)
                .body("productId", notNullValue())
                .body("productName", equalTo("신용대출"))
                .body("interestRate", equalTo(5.5f));

        given()
            .when()
                .get("/v1/products/1")
            .then()
                .statusCode(200)
                .body("productName", equalTo("신용대출"));
    }
}