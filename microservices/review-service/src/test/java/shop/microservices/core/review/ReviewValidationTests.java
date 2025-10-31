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
    public void testProductIdMustBeNonNegative() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test negative productId
            var violations = validator.validate(
                    new ReviewEntity(-1, 1, "Author", "Subject", 
                            "Lorem ipsum dolor sit amet, consetetur sadipscing elit", 4, LocalDate.now()));

            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath().toString())
                    .contains("productId");
            assertThat(violations)
                    .extracting(it -> it.getMessage())
                    .contains("must be greater than or equal to 0");
        }
    }

    @Test
    public void testReviewIdMustBeNonNegative() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test negative reviewId
            var violations = validator.validate(
                    new ReviewEntity(1, -1, "Author", "Subject", 
                            "Lorem ipsum dolor sit amet, consetetur sadipscing elit", 4, LocalDate.now()));

            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath().toString())
                    .contains("reviewId");
            assertThat(violations)
                    .extracting(it -> it.getMessage())
                    .contains("must be greater than or equal to 0");
        }
    }

    @Test
    public void testAuthorMustNotBeBlank() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test blank author
            var violations = validator.validate(
                    new ReviewEntity(1, 1, "", "Subject", 
                            "Lorem ipsum dolor sit amet, consetetur sadipscing elit", 4, LocalDate.now()));

            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath().toString())
                    .contains("author");
            assertThat(violations)
                    .extracting(it -> it.getMessage())
                    .contains("must not be blank");

            // Test null author (should also fail)
            violations = validator.validate(
                    new ReviewEntity(1, 1, null, "Subject", 
                            "Lorem ipsum dolor sit amet, consetetur sadipscing elit", 4, LocalDate.now()));

            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath().toString())
                    .contains("author");
        }
    }

    @Test
    public void testSubjectMustNotBeBlank() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test blank subject
            var violations = validator.validate(
                    new ReviewEntity(1, 1, "Author", "", 
                            "Lorem ipsum dolor sit amet, consetetur sadipscing elit", 4, LocalDate.now()));

            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath().toString())
                    .contains("subject");
            assertThat(violations)
                    .extracting(it -> it.getMessage())
                    .contains("must not be blank");

            // Test whitespace-only subject (should also fail)
            violations = validator.validate(
                    new ReviewEntity(1, 1, "Author", "   ", 
                            "Lorem ipsum dolor sit amet, consetetur sadipscing elit", 4, LocalDate.now()));

            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath().toString())
                    .contains("subject");
        }
    }

    @Test
    public void testContentMustNotBeBlank() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test blank content
            var violations = validator.validate(
                    new ReviewEntity(1, 1, "Author", "Subject", "", 4, LocalDate.now()));

            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath().toString())
                    .contains("content");
            
            // Should have both "must not be blank" and size constraint violations
            assertTrue(violations.size() >= 1);
        }
    }

    @Test
    public void testContentMustBeAtLeast50Characters() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test content with exactly 49 characters (should fail)
            String shortContent = "a".repeat(49); // 49 characters
            var violations = validator.validate(
                    new ReviewEntity(1, 1, "Author", "Subject", shortContent, 4, LocalDate.now()));

            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath().toString())
                    .contains("content");
            assertThat(violations)
                    .extracting(it -> it.getMessage())
                    .anyMatch(msg -> msg.contains("size must be between 50 and 200"));
        }
    }

    @Test
    public void testContentMustNotExceed200Characters() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test content with exactly 201 characters (should fail)
            String longContent = "a".repeat(201); // 201 characters
            var violations = validator.validate(
                    new ReviewEntity(1, 1, "Author", "Subject", longContent, 4, LocalDate.now()));

            assertFalse(violations.isEmpty());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath().toString())
                    .contains("content");
            assertThat(violations)
                    .extracting(it -> it.getMessage())
                    .anyMatch(msg -> msg.contains("size must be between 50 and 200"));
        }
    }

    @Test
    public void testValidReviewEntityWithMinimumValues() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test with minimum valid values: productId=0, reviewId=0, content=50 chars
            String validContent = "a".repeat(50); // Exactly 50 characters
            var violations = validator.validate(
                    new ReviewEntity(0, 0, "Author", "Subject", validContent, 3, LocalDate.now()));

            assertTrue(violations.isEmpty(), "Valid entity should have no violations");
        }
    }

    @Test
    public void testValidReviewEntityWithMaximumValues() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test with maximum valid values: content=200 chars, rating=5
            String validContent = "a".repeat(200); // Exactly 200 characters
            var violations = validator.validate(
                    new ReviewEntity(1000, 1000, "John Doe", "Great Product", validContent, 5, LocalDate.now()));

            assertTrue(violations.isEmpty(), "Valid entity should have no violations");
        }
    }

    @Test
    public void testMultipleValidationErrors() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test entity with multiple validation errors
            var violations = validator.validate(
                    new ReviewEntity(-1, -1, "", "", "short", 0, null));

            assertFalse(violations.isEmpty());
            assertTrue(violations.size() >= 6, "Should have multiple validation errors");
            
            // Verify specific violations exist
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath().toString())
                    .contains("productId", "reviewId", "author", "subject", "content", "rating", "date");
        }
    }
}
