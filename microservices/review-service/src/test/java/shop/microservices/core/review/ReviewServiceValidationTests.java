package shop.microservices.core.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import shop.api.core.review.Review;
import shop.api.core.review.ReviewService;
import shop.api.exceptions.InvalidInputException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { "eureka.client.enabled=false" })
public class ReviewServiceValidationTests extends MySqlTestBase {

    @Autowired
    private ReviewService reviewService;

    @Test
    public void testCreateReviewWithValidData() {
        Review validReview = new Review(
                0,  // productId >= 0 ✓
                1,  // reviewId >= 0 ✓
                "John Doe",  // author not blank ✓
                "Great Product",  // subject not blank ✓
                "This is a very detailed review with more than fifty characters to meet the validation requirements.",  // content > 50 chars ✓
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        // This should not throw any exception
        reviewService.createReview(validReview).block();
    }

    @Test
    public void testCreateReviewWithNegativeProductId() {
        Review invalidReview = new Review(
                -1,  // Invalid: productId < 0
                1,
                "John Doe",
                "Great Product",
                "This is a very detailed review with more than fifty characters to meet the validation requirements.",
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> reviewService.createReview(invalidReview).block()
        );
        assertTrue(exception.getMessage().contains("Invalid productId: -1"));
    }

    @Test
    public void testCreateReviewWithNegativeReviewId() {
        Review invalidReview = new Review(
                1,
                -1,  // Invalid: reviewId < 0
                "John Doe",
                "Great Product",
                "This is a very detailed review with more than fifty characters to meet the validation requirements.",
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> reviewService.createReview(invalidReview).block()
        );
        assertTrue(exception.getMessage().contains("must be greater than or equal to 0"));
    }

    @Test
    public void testCreateReviewWithBlankAuthor() {
        Review invalidReview = new Review(
                1,
                1,
                "",  // Invalid: author is blank
                "Great Product",
                "This is a very detailed review with more than fifty characters to meet the validation requirements.",
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> reviewService.createReview(invalidReview).block()
        );
        assertTrue(exception.getMessage().contains("must not be blank"));
    }

    @Test
    public void testCreateReviewWithNullAuthor() {
        Review invalidReview = new Review(
                1,
                1,
                null,  // Invalid: author is null
                "Great Product",
                "This is a very detailed review with more than fifty characters to meet the validation requirements.",
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> reviewService.createReview(invalidReview).block()
        );
        assertTrue(exception.getMessage().contains("must not be blank"));
    }

    @Test
    public void testCreateReviewWithBlankSubject() {
        Review invalidReview = new Review(
                1,
                1,
                "John Doe",
                "",  // Invalid: subject is blank
                "This is a very detailed review with more than fifty characters to meet the validation requirements.",
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> reviewService.createReview(invalidReview).block()
        );
        assertTrue(exception.getMessage().contains("must not be blank"));
    }

    @Test
    public void testCreateReviewWithNullSubject() {
        Review invalidReview = new Review(
                1,
                1,
                "John Doe",
                null,  // Invalid: subject is null
                "This is a very detailed review with more than fifty characters to meet the validation requirements.",
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> reviewService.createReview(invalidReview).block()
        );
        assertTrue(exception.getMessage().contains("must not be blank"));
    }

    @Test
    public void testCreateReviewWithBlankContent() {
        Review invalidReview = new Review(
                1,
                1,
                "John Doe",
                "Great Product",
                "",  // Invalid: content is blank
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> reviewService.createReview(invalidReview).block()
        );
        assertTrue(exception.getMessage().contains("size must be between 50 and 200"));
    }

    @Test
    public void testCreateReviewWithEmptyContent() {
        Review invalidReview = new Review(
                1,
                1,
                "John Doe",
                "Great Product",
                "",
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                        InvalidInputException.class,
                () -> reviewService.createReview(invalidReview).block()
        );
        assertTrue(exception.getMessage().contains("size must be between 50 and 200"));
    }

    @Test
    public void testCreateReviewWithShortContent() {
        Review invalidReview = new Review(
                1,
                1,
                "John Doe",
                "Great Product",
                "Short content",  // Invalid: content < 50 characters
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> reviewService.createReview(invalidReview).block()
        );
        assertTrue(exception.getMessage().contains("size must be between 50 and 200"));
    }

    @Test
    public void testCreateReviewWithExactly50Characters() {
        Review validReview = new Review(
                1,
                1,
                "John Doe",
                "Great Product",
                "12345678901234567890123456789012345678901234567890",  // Exactly 50 characters
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        // This should not throw any exception
        reviewService.createReview(validReview).block();
    }

    @Test
    public void testCreateReviewWithMultipleValidationErrors() {
        Review invalidReview = new Review(
                -1,  // Invalid: productId < 0
                -1,  // Invalid: reviewId < 0
                "",  // Invalid: author is blank
                "",  // Invalid: subject is blank
                "Short",  // Invalid: content < 50 characters
                5,
                LocalDate.now(),
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> reviewService.createReview(invalidReview).block()
        );

        // Should contain at least one validation error message
        String message = exception.getMessage();
        assertTrue(message.contains("Invalid productId") || 
                   message.contains("must be greater than or equal to 0") ||
                   message.contains("must not be blank") ||
                   message.contains("size must be between 50 and 200"));
    }

    @Test
    public void testCreateReviewWithBoundaryValues() {
        // Test with minimum valid values
        Review validReview = new Review(
                0,  // Minimum valid productId
                0,  // Minimum valid reviewId
                "A",  // Minimum valid author (1 character)
                "S",  // Minimum valid subject (1 character)
                "12345678901234567890123456789012345678901234567890",  // Exactly 50 characters
                1,  // Minimum valid rating
                LocalDate.now(),
                "serviceAddress"
        );

        // This should not throw any exception
        reviewService.createReview(validReview).block();
    }
}
