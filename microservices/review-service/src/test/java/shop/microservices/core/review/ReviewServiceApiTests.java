package shop.microservices.core.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import shop.api.core.review.Review;
import shop.api.event.Event;
import shop.api.exceptions.InvalidInputException;
import shop.microservices.core.review.persistence.ReviewRepository;

import java.time.LocalDate;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static shop.api.event.Event.Type.CREATE;
import static shop.api.event.Event.Type.DELETE;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.cloud.stream.defaultBinder=rabbit",
        "logging.level.shop=DEBUG",
        "eureka.client.enabled=false"})
class ReviewServiceApiTests extends MySqlTestBase {

    private static final String REVIEW_CONTENT = "Lorem ipsum dolor sit amet, consetetur sadipscingw";

    @Autowired
    private WebTestClient client;

    @Autowired
    private ReviewRepository repository;

    @Autowired
    @Qualifier("messageProcessor")
    private Consumer<Event<Integer, Review>> messageProcessor;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    void getReviewsByProductId() {
        int productId = 1;

        assertEquals(0, repository.findByProductId(productId).size());

        sendCreateReviewEvent(productId, 1);
        sendCreateReviewEvent(productId, 2);
        sendCreateReviewEvent(productId, 3);

        assertEquals(3, repository.findByProductId(productId).size());

        getAndVerifyReviewsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].reviewId").isEqualTo(3);
    }

    @Test
    void duplicateError() {

        int productId = 1;
        int reviewId = 1;

        assertEquals(0, repository.count());

        sendCreateReviewEvent(productId, reviewId);

        assertEquals(1, repository.count());

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> sendCreateReviewEvent(productId, reviewId),
                "Expected a InvalidInputException here!");
        assertEquals("Duplicate key, Product Id: 1, Review Id:1", thrown.getMessage());

        assertEquals(1, repository.count());
    }

    @Test
    void deleteReviews() {

        int productId = 1;
        int reviewId = 1;

        sendCreateReviewEvent(productId, reviewId);
        assertEquals(1, repository.findByProductId(productId).size());

        sendDeleteReviewEvent(productId);
        assertEquals(0, repository.findByProductId(productId).size());

        sendDeleteReviewEvent(productId);
    }

    @Test
    void getReviewsMissingParameter() {

        getAndVerifyReviewsByProductId("", BAD_REQUEST);
    }

    @Test
    void getReviewsInvalidParameter() {
        getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST);
    }

    @Test
    void getReviewsNotFound() {
        getAndVerifyReviewsByProductId("?productId=213", OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getReviewsInvalidParameterNegativeValue() {
        int productIdInvalid = -1;

        getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }

    @Test
    void getReviewsWithProductIdZero() {
        // Test that productId = 0 is now valid (should return OK, not UNPROCESSABLE_ENTITY)
        getAndVerifyReviewsByProductId("?productId=0", OK)
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void createReviewWithNegativeProductId() {
        String invalidReviewJson = """
                {
                    "productId": -1,
                    "reviewId": 1,
                    "author": "Author",
                    "subject": "Subject",
                    "content": "%s",
                    "serviceAddress": "SA"
                }
                """.formatted(REVIEW_CONTENT);

        client.post()
                .uri("/review")
                .contentType(APPLICATION_JSON)
                .bodyValue(invalidReviewJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createReviewWithNegativeReviewId() {
        String invalidReviewJson = """
                {
                    "productId": 1,
                    "reviewId": -1,
                    "author": "Author",
                    "subject": "Subject",
                    "content": "%s",
                    "serviceAddress": "SA"
                }
                """.formatted(REVIEW_CONTENT);

        client.post()
                .uri("/review")
                .contentType(APPLICATION_JSON)
                .bodyValue(invalidReviewJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createReviewWithBlankAuthor() {
        String invalidReviewJson = """
                {
                    "productId": 1,
                    "reviewId": 1,
                    "author": "",
                    "subject": "Subject",
                    "content": "%s",
                    "serviceAddress": "SA"
                }
                """.formatted(REVIEW_CONTENT);

        client.post()
                .uri("/review")
                .contentType(APPLICATION_JSON)
                .bodyValue(invalidReviewJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createReviewWithBlankSubject() {
        String invalidReviewJson = """
                {
                    "productId": 1,
                    "reviewId": 1,
                    "author": "Author",
                    "subject": "",
                    "content": "%s",
                    "serviceAddress": "SA"
                }
                """.formatted(REVIEW_CONTENT);

        client.post()
                .uri("/review")
                .contentType(APPLICATION_JSON)
                .bodyValue(invalidReviewJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createReviewWithShortContent() {
        String invalidReviewJson = """
                {
                    "productId": 1,
                    "reviewId": 1,
                    "author": "Author",
                    "subject": "Subject",
                    "content": "Too short",
                    "serviceAddress": "SA"
                }
                """;

        client.post()
                .uri("/review")
                .contentType(APPLICATION_JSON)
                .bodyValue(invalidReviewJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createReviewWithLongContent() {
        String longContent = "a".repeat(201);
        String invalidReviewJson = """
                {
                    "productId": 1,
                    "reviewId": 1,
                    "author": "Author",
                    "subject": "Subject",
                    "content": "%s",
                    "serviceAddress": "SA"
                }
                """.formatted(longContent);

        client.post()
                .uri("/review")
                .contentType(APPLICATION_JSON)
                .bodyValue(invalidReviewJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createValidReview() {
        String validReviewJson = """
                {
                    "productId": 1,
                    "reviewId": 1,
                    "author": "John Doe",
                    "subject": "Great product",
                    "content": "%s",
                    "rating": 5,
                    "date": "2023-10-25",
                    "serviceAddress": "SA"
                }
                """.formatted(REVIEW_CONTENT);

        client.post()
                .uri("/review")
                .contentType(APPLICATION_JSON)
                .bodyValue(validReviewJson)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void createReviewWithInvalidRating() {
        String invalidReviewJson = """
                {
                    "productId": 1,
                    "reviewId": 1,
                    "author": "Author",
                    "subject": "Subject",
                    "content": "%s",
                    "rating": "R",
                    "serviceAddress": "SA"
                }
                """.formatted(REVIEW_CONTENT);

        client.post()
                .uri("/review")
                .contentType(APPLICATION_JSON)
                .bodyValue(invalidReviewJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createReviewWithMultipleValidationErrors() {
        String invalidReviewJson = """
                {
                    "productId": -1,
                    "reviewId": -1,
                    "author": "",
                    "subject": "",
                    "content": "short",
                    "rating": 1,
                    "serviceAddress": "SA"
                }
                """;

        client.post()
                .uri("/review")
                .contentType(APPLICATION_JSON)
                .bodyValue(invalidReviewJson)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @SuppressWarnings("SameParameterValue")
    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
        return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus) {
        return client.get()
                .uri("/review" + productIdQuery)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateReviewEvent(int productId, int reviewId) {
        Review review = new Review(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, REVIEW_CONTENT + reviewId, 4, LocalDate.now(), "SA");
        Event<Integer, Review> event = new Event<>(CREATE, productId, review);
        messageProcessor.accept(event);
    }

    private void sendDeleteReviewEvent(int productId) {
        Event<Integer, Review> event = new Event<>(DELETE, productId, null);
        messageProcessor.accept(event);
    }
}