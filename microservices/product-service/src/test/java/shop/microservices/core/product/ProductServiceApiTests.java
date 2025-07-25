package shop.microservices.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import shop.api.core.product.Product;
import shop.microservices.core.product.persistence.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@AutoConfigureMockMvc
@SpringBootTest
public class ProductServiceApiTests {


    @Autowired
    private WebTestClient client;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
    }

    @Test
    void getProductById() {
        int productId = 1;

        postAndVerifyProduct(productId, OK);

        assertTrue(repository.findByProductId(productId).blockOptional().isPresent());

        getAndVerifyProduct(productId, OK).jsonPath("$.productId").isEqualTo(productId);
    }

    @Test
    void duplicateError() {
        int productId = 1;

        postAndVerifyProduct(productId, OK);

        assertTrue(repository.findByProductId(productId).blockOptional().isPresent());

        postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + productId);
    }

    @Test
    void deleteProduct() {
        int productId = 1;

        postAndVerifyProduct(productId, OK);
        assertTrue(repository.findByProductId(productId).blockOptional().isPresent());

        deleteAndVerifyProduct(productId, OK);
        assertFalse(repository.findByProductId(productId).blockOptional().isPresent());

        deleteAndVerifyProduct(productId, OK);
    }

    @Test
    void getProductInvalidParameterString() {
        getAndVerifyProduct("/no-integer", BAD_REQUEST);
    }

    @Test
    void getProductInvalidParameterNegativeValue() {
        int productIdInvalid = -1;

        getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/product" + productIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        Product product = new Product(productId, "Name " + productId, productId, "SA");
        return client.post()
                .uri("/product")
                .body(just(product), Product.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void deleteAndVerifyProduct(int productId, @SuppressWarnings("SameParameterValue") HttpStatus expectedStatus) {
        client.delete()
                .uri("/product/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}
