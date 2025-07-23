package shop.microservices.core.review;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import shop.microservices.core.review.persistence.ReviewEntity;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ReviewValidationTests {

    @Test
    public void testReviewEntityValidationNegative() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            var violations = validator.validate(
                    new ReviewEntity(-1, -1, "", "", "test"));

            assertFalse(violations.isEmpty());
            assertEquals(5, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .containsExactlyInAnyOrder(
                            "productId must be greater than or equal to 0",
                            "reviewId must be greater than or equal to 0",
                            "author must not be blank",
                            "content size must be between 50 and 200",
                            "subject must not be blank");
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
                            "Lorem ipsum dolor sit amet, consetetur sadipscingw"));

            assertTrue(violations.isEmpty());
        }
    }
}
