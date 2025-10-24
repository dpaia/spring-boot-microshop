package shop.microservices.core.review;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import shop.microservices.core.review.persistence.ReviewEntity;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ReviewValidationTests {

    @Test
    public void testReviewEntityValidationNegative() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            var violations = validator.validate(
                    new ReviewEntity(-1, -1, "", "", "test", -1, null));

            assertFalse(violations.isEmpty());
            assertEquals(7, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .containsExactlyInAnyOrder(
                            "productId must be greater than or equal to 0",
                            "reviewId must be greater than or equal to 0",
                            "author must not be blank",
                            "content size must be between 50 and 200",
                            "subject must not be blank",
                            "date must not be null",
                            "rating must be between 1 and 5");
        }
    }

    @Test
    public void testReviewEntityValidationPositive() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            var violations = validator.validate(
                    new ReviewEntity(
                            0,
                            0,
                            "John Snow",
                            "Test",
                            "Lorem ipsum dolor sit amet, consetetur sadipscingw",
                            4,
                            LocalDate.now()));

            assertTrue(violations.isEmpty());
        }
    }

    @Test
    public void testProductIdBoundaryValues() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

        // Test productId = 0 (minimum valid value)
        var violations = validator.validate(
                new ReviewEntity(
                        0,  // Valid: productId >= 0
                        1,
                        "John Snow",
                        "Test Subject",
                        "12345678901234567890123456789012345678901234567890",  // Exactly 50 characters
                        4,
                        LocalDate.now()));
        assertTrue(violations.isEmpty());            // Test productId = -1 (invalid)
            violations = validator.validate(
                    new ReviewEntity(
                            -1,  // Invalid: productId < 0
                            1,
                            "John Snow",
                            "Test Subject",
                            "12345678901234567890123456789012345678901234567890",  // Exactly 50 characters
                            4,
                            LocalDate.now()));
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("productId must be greater than or equal to 0");
        }
    }

    @Test
    public void testReviewIdBoundaryValues() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test reviewId = 0 (minimum valid value)
            var violations = validator.validate(
                    new ReviewEntity(
                            1,
                            0,  // Valid: reviewId >= 0
                            "John Snow",
                            "Test Subject",
                            "12345678901234567890123456789012345678901234567890",  // Exactly 50 characters
                            4,
                            LocalDate.now()));
            assertTrue(violations.isEmpty());

            // Test reviewId = -1 (invalid)
            violations = validator.validate(
                    new ReviewEntity(
                            1,
                            -1,  // Invalid: reviewId < 0
                            "John Snow",
                            "Test Subject",
                            "12345678901234567890123456789012345678901234567890",  // Exactly 50 characters
                            4,
                            LocalDate.now()));
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("reviewId must be greater than or equal to 0");
        }
    }

    @Test
    public void testAuthorValidation() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test blank author
            var violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            "",  // Invalid: blank author
                            "Test Subject",
                            "12345678901234567890123456789012345678901234567890",
                            4,
                            LocalDate.now()));
            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .anyMatch(msg -> msg.contains("author must not be blank"));

            // Test null author
            violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            null,  // Invalid: null author
                            "Test Subject",
                            "12345678901234567890123456789012345678901234567890",
                            4,
                            LocalDate.now()));
            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .anyMatch(msg -> msg.contains("author must not be blank"));

            // Test whitespace-only author
            violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            "   ",  // Invalid: whitespace-only author
                            "Test Subject",
                            "12345678901234567890123456789012345678901234567890",
                            4,
                            LocalDate.now()));
            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .anyMatch(msg -> msg.contains("author must not be blank"));
        }
    }

    @Test
    public void testSubjectValidation() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test blank subject
            var violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            "John Snow",
                            "",  // Invalid: blank subject
                            "12345678901234567890123456789012345678901234567890",
                            4,
                            LocalDate.now()));
            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .anyMatch(msg -> msg.contains("subject must not be blank"));

            // Test null subject
            violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            "John Snow",
                            null,  // Invalid: null subject
                            "12345678901234567890123456789012345678901234567890",
                            4,
                            LocalDate.now()));
            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .anyMatch(msg -> msg.contains("subject must not be blank"));
        }
    }

    @Test
    public void testContentSizeValidation() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test content with exactly 50 characters (minimum valid)
            var violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            "John Snow",
                            "Test Subject",
                            "12345678901234567890123456789012345678901234567890",  // Exactly 50 characters
                            4,
                            LocalDate.now()));
            assertTrue(violations.isEmpty());

            // Test content with 49 characters (invalid)
            violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            "John Snow",
                            "Test Subject",
                            "1234567890123456789012345678901234567890123456789",  // 49 characters
                            4,
                            LocalDate.now()));
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("content size must be between 50 and 200");

            // Test content with exactly 200 characters (maximum valid)
            String content200 = "1234567890".repeat(20);  // 200 characters
            violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            "John Snow",
                            "Test Subject",
                            content200,
                            4,
                            LocalDate.now()));
            assertTrue(violations.isEmpty());

            // Test content with 201 characters (invalid)
            String content201 = content200 + "1";  // 201 characters
            violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            "John Snow",
                            "Test Subject",
                            content201,
                            4,
                            LocalDate.now()));
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("content size must be between 50 and 200");

            // Test blank content
            violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            "John Snow",
                            "Test Subject",
                            "",  // Invalid: blank content
                            4,
                            LocalDate.now()));
            assertEquals(2, violations.size());  // Both @NotBlank and @Size violations
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .containsExactlyInAnyOrder(
                            "content must not be blank",
                            "content size must be between 50 and 200");

            // Test null content
            violations = validator.validate(
                    new ReviewEntity(
                            1,
                            1,
                            "John Snow",
                            "Test Subject",
                            null,  // Invalid: null content
                            4,
                            LocalDate.now()));
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("content must not be blank");
        }
    }

    @Test
    public void testMultipleValidationErrors() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test entity with multiple validation errors
            var violations = validator.validate(
                    new ReviewEntity(
                            -5,  // Invalid: productId < 0
                            -3,  // Invalid: reviewId < 0
                            "",  // Invalid: blank author
                            "",  // Invalid: blank subject
                            "short",  // Invalid: content < 50 characters
                            0,  // Invalid: rating < 1
                            null));  // Invalid: null date

            assertEquals(7, violations.size());  // Short content only triggers @Size, not @NotBlank
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .containsExactlyInAnyOrder(
                            "productId must be greater than or equal to 0",
                            "reviewId must be greater than or equal to 0",
                            "author must not be blank",
                            "subject must not be blank",
                            "content size must be between 50 and 200",
                            "rating must be between 1 and 5",
                            "date must not be null");
        }
    }
}
