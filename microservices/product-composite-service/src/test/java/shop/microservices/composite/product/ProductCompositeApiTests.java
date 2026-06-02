package shop.microservices.composite.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import shop.api.core.product.Product;
import shop.api.core.recommendation.Recommendation;
import shop.api.core.review.Review;
import shop.api.exceptions.InvalidInputException;
import shop.api.exceptions.NotFoundException;
import shop.microservices.composite.product.services.ProductCompositeIntegration;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"eureka.client.enabled=false"})
class ProductCompositeApiTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private ProductCompositeIntegration compositeIntegration;

    @BeforeEach
    void setUp() {
        when(compositeIntegration.getProduct(PRODUCT_ID_OK))
                .thenReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1, "mock-address")));
        when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
                .thenReturn(Flux.just(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")));
        when(compositeIntegration.getReviews(PRODUCT_ID_OK))
                .thenReturn(Flux.just(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", 4, LocalDate.now(), "mock address")));
        when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
                .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
        when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
                .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

        when(compositeIntegration.getAllProducts())
                .thenReturn(Flux.just(
                        new Product(5, "water", 1, "mock-address"),
                        new Product(6, "bread", 2, "mock-address"),
                        new Product(7, "sugar", 3, "mock-address")));
        when(compositeIntegration.getRecommendations(5))
                .thenReturn(Flux.just(
                        new Recommendation(5, 5, "author", 5, "content", "mock address")
                ));
        when(compositeIntegration.getRecommendations(6)).thenReturn(Flux.just());
        when(compositeIntegration.getRecommendations(7)).thenReturn(Flux.just());
        when(compositeIntegration.getReviews(5)).thenReturn(Flux.just(
                new Review(5, 5, "author", "subject", "content", 5, LocalDate.now(), "mock address")
        ));
        when(compositeIntegration.getReviews(6)).thenReturn(Flux.just());
        when(compositeIntegration.getReviews(7)).thenReturn(Flux.just());
    }

    @Test
    void getProductById() {
        getAndVerifyProduct(PRODUCT_ID_OK, OK)
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    void getProductInvalidInput() {
        getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY);
    }

    @Test
    void getProductNotFound() {
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getReviewsByProductId() {
        client.get()
                .uri("/product-composite/reviews/" + PRODUCT_ID_OK)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(OK)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].rating").isEqualTo(4);
    }

    @Test
    void getAllProducts() {
        client.get()
                .uri("/product-composite")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(OK)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].name").isEqualTo("water")
                .jsonPath("$[0].recommendations[0].author").isEqualTo("author")
                .jsonPath("$[0].reviews[0].author").isEqualTo("author")
                .jsonPath("$[1].name").isEqualTo("bread")
                .jsonPath("$[2].name").isEqualTo("sugar");
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return client.get()
                .uri("/product-composite/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }
}
