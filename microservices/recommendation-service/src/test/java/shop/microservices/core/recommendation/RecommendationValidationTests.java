package shop.microservices.core.recommendation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import shop.microservices.core.recommendation.persistence.RecommendationEntity;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class RecommendationValidationTests {

    @Test
    public void testRecommendationEntityValidationNegative() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            var violations = validator.validate(
                    new RecommendationEntity(-1, -1, "", -1, ""));

            assertFalse(violations.isEmpty());
            assertEquals(5, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .containsExactlyInAnyOrder(
                            "recommendationId must be greater than or equal to 0",
                            "content size must be between 50 and 200",
                            "rating must be between 1 and 5",
                            "productId must be greater than or equal to 0",
                            "author must not be blank");
        }
    }

    @Test
    public void testRecommendationEntityValidationPositive() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            var violations = validator.validate(
                    new RecommendationEntity(
                            0,
                            0,
                            "John Snow",
                            4,
                            "Lorem ipsum dolor sit amet, consetetur sadipscingw"));

            assertTrue(violations.isEmpty());
        }
    }
}
