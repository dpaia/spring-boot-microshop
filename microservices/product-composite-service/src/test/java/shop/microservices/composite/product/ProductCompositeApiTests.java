package shop.microservices.composite.product;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.AbstractJsonContentAssert;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import shop.api.composite.product.ProductAggregate;
import shop.api.composite.product.RecommendationSummary;
import shop.api.composite.product.ReviewSummary;
import shop.api.core.product.Product;
import shop.api.core.recommendation.Recommendation;
import shop.api.core.review.Review;
import shop.api.exceptions.InvalidInputException;
import shop.microservices.composite.product.services.ProductCompositeIntegration;

import java.time.LocalDate;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeApiTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private ProductCompositeIntegration compositeIntegration;

    @BeforeEach
    void setUp() {
        when(compositeIntegration.getProduct(PRODUCT_ID_OK))
                .thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
        when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
                .thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")));
        when(compositeIntegration.getReviews(PRODUCT_ID_OK))
                .thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", LocalDate.now(), "mock address")));
        when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
                .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }

    @Test
    void createCompositeProduct1() {
        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1, null, null, null);

        postAndVerifyProduct(compositeProduct, OK);
    }

    @Test
    void createCompositeProduct2() {
        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
                singletonList(new RecommendationSummary(1, "a", 1, "c")),
                singletonList(new ReviewSummary(1, "a", "s", "c")), null);

        postAndVerifyProduct(compositeProduct, OK);
    }

    @Test
    void deleteCompositeProduct() {
        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
                singletonList(new RecommendationSummary(1, "a", 1, "c")),
                singletonList(new ReviewSummary(1, "a", "s", "c")), null);

        postAndVerifyProduct(compositeProduct, OK);

        deleteAndVerifyProduct(compositeProduct.productId(), OK);
        deleteAndVerifyProduct(compositeProduct.productId(), OK);
    }

    @Test
    void getProductById() {
        getAndVerifyProduct(PRODUCT_ID_OK, OK)
                .hasPathSatisfying("$.productId", it -> it.assertThat().isEqualTo(PRODUCT_ID_OK))
                .hasPathSatisfying("$.recommendations.length()", it -> it.assertThat().isEqualTo(1))
                .hasPathSatisfying("$.reviews.length()", it -> it.assertThat().isEqualTo(1));
    }

    @Test
    void getProductInvalidInput() {
        getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY);
    }

    private AbstractJsonContentAssert<?> getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return mockMvcTester.get()
                .uri("/product-composite/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(expectedStatus)
                .bodyJson();
    }

    @SuppressWarnings("SameParameterValue")
    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
        mockMvcTester.post()
                .uri("/product-composite")
                .contentType(APPLICATION_JSON)
                .content(new Gson().toJson(compositeProduct))
                .exchange()
                .assertThat()
                .hasStatus(expectedStatus);
    }

    @SuppressWarnings("SameParameterValue")
    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        mockMvcTester.delete()
                .uri("/product-composite/" + productId)
                .contentType(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(expectedStatus);
    }
}
