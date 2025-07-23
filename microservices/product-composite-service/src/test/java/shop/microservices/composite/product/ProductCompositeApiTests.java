package shop.microservices.composite.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import shop.api.core.product.Product;
import shop.api.core.recommendation.Recommendation;
import shop.api.core.review.Review;
import shop.api.exceptions.InvalidInputException;
import shop.microservices.composite.product.services.ProductCompositeIntegration;

import java.time.LocalDate;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest
public class ProductCompositeApiTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_INVALID = -1;

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private ProductCompositeIntegration compositeIntegration;

    @BeforeEach
    void setUp() {
        when(compositeIntegration.getProduct(PRODUCT_ID_OK))
                .thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
        when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
                .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));

        when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
                .thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")));

        when(compositeIntegration.getReviews(PRODUCT_ID_OK))
                .thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", LocalDate.now(), "mock address")));
    }

    @Test
    public void testProductCompositeServiceApi() {
        var jsonBody = mockMvcTester.get()
                .uri("/product-composite/" + PRODUCT_ID_OK)
                .accept(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(OK)
                .hasContentType(APPLICATION_JSON)
                .bodyJson();

        jsonBody.extractingPath("$.productId").isEqualTo(PRODUCT_ID_OK);
        jsonBody.extractingPath("$.name").isEqualTo("name");
        jsonBody.extractingPath("$.weight").isEqualTo(1);

        jsonBody.extractingPath("$.recommendations[0].recommendationId").isEqualTo(1);
        jsonBody.extractingPath("$.recommendations[0].author").isEqualTo("author");
        jsonBody.extractingPath("$.recommendations[0].rate").isEqualTo(1);
        jsonBody.extractingPath("$.recommendations[0].content").isEqualTo("content");

        jsonBody.extractingPath("$.reviews[0].reviewId").isEqualTo(1);
        jsonBody.extractingPath("$.reviews[0].author").isEqualTo("author");
        jsonBody.extractingPath("$.reviews[0].subject").isEqualTo("subject");
        jsonBody.extractingPath("$.reviews[0].content").isEqualTo("content");
    }

    @Test
    void testBadRequestForWrongParameterType() {
        mockMvcTester.get()
                .uri("/product-composite/no-integer")
                .accept(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testUnprocessableEntityForNegativeParameter() {
        mockMvcTester.get()
                .uri("/product-composite/" + PRODUCT_ID_INVALID)
                .accept(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
