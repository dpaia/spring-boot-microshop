package shop.microservices.core.product;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import shop.microservices.core.product.persistence.ProductEntity;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ProductValidationTests {

    @Test
    public void testReviewEntityValidationNegative() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            var violations = validator.validate(
                    new ProductEntity(-1, "", 0));

            assertFalse(violations.isEmpty());
            assertEquals(3, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .containsExactlyInAnyOrder(
                            "name size must be between 5 and 100",
                            "weight must be greater than or equal to 1",
                            "productId must be greater than or equal to 1");
        }
    }

    @Test
    public void testReviewEntityValidationPositive() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            var violations = validator.validate(
                    new ProductEntity(
                            1,
                            "Water",
                            4));

            assertTrue(violations.isEmpty());
        }
    }
}
