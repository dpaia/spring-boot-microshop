package shop.microservices.core.recommendation;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.json.AbstractJsonContentAssert;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import shop.api.core.recommendation.Recommendation;
import shop.microservices.core.recommendation.persistence.RecommendationRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class RecommendationServiceApiTests extends MongoDbTestBase {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private RecommendationRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    void getRecommendationsByProductId() {
        int productId = 1;

        postAndVerifyRecommendation(productId, 1, OK);
        postAndVerifyRecommendation(productId, 2, OK);
        postAndVerifyRecommendation(productId, 3, OK);

        assertEquals(3, repository.findByProductId(productId).size());

        getAndVerifyRecommendationsByProductId(productId, OK)
                .hasPathSatisfying("$.length()", it -> it.assertThat().isEqualTo(3))
                .hasPathSatisfying("$[2].productId", it -> it.assertThat().isEqualTo(productId))
                .hasPathSatisfying("$[2].recommendationId", it -> it.assertThat().isEqualTo(3));
    }

    @Test
    void duplicateError() {
        int productId = 1;
        int recommendationId = 1;

        postAndVerifyRecommendation(productId, recommendationId, OK)
                .hasPathSatisfying("$.productId", it -> it.assertThat().isEqualTo(productId))
                .hasPathSatisfying("$.recommendationId", it -> it.assertThat().isEqualTo(recommendationId));

        assertEquals(1, repository.count());

        postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY)
                .hasPathSatisfying("$.path", it -> it.assertThat().isEqualTo("/recommendation"))
                .hasPathSatisfying("$.message", it -> it.assertThat().isEqualTo("Duplicate key, Product Id: 1, Recommendation Id:1"));

        assertEquals(1, repository.count());
    }

    @Test
    void deleteRecommendations() {
        int productId = 1;
        int recommendationId = 1;

        postAndVerifyRecommendation(productId, recommendationId, OK);
        assertEquals(1, repository.findByProductId(productId).size());

        deleteAndVerifyRecommendationsByProductId(productId, OK);
        assertEquals(0, repository.findByProductId(productId).size());

        deleteAndVerifyRecommendationsByProductId(productId, OK);
    }

    @Test
    void getRecommendationsMissingParameter() {
        getAndVerifyRecommendationsByProductId("", BAD_REQUEST);
    }

    @Test
    void getRecommendationsInvalidParameter() {
        getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST);
    }

    @Test
    void getRecommendationsNotFound() {
        getAndVerifyRecommendationsByProductId("?productId=113", OK)
                .hasPathSatisfying("$.length()", it -> it.assertThat().isEqualTo(0));
    }

    @Test
    void getRecommendationsInvalidParameterNegativeValue() {
        int productIdInvalid = -1;

        getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
                .hasPathSatisfying("$.path", it -> it.assertThat().isEqualTo("/recommendation"))
                .hasPathSatisfying("$.message", it -> it.assertThat().isEqualTo("Invalid productId: " + productIdInvalid));
    }

    private AbstractJsonContentAssert<?> getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
        return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
    }

    private AbstractJsonContentAssert<?> getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
        return mockMvcTester.get()
                .uri("/recommendation" + productIdQuery)
                .contentType(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(expectedStatus)
                .bodyJson();
    }

    private AbstractJsonContentAssert<?> postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus) {
        Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, recommendationId, "Content " + recommendationId, "SA");
        return mockMvcTester.post()
                .uri("/recommendation")
                .contentType(APPLICATION_JSON)
                .content(new Gson().toJson(recommendation))
                .exchange()
                .assertThat()
                .hasStatus(expectedStatus)
                .bodyJson();
    }

    private void deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
        mockMvcTester.delete()
                .uri("/recommendation?productId=" + productId)
                .contentType(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(expectedStatus);
    }
}
