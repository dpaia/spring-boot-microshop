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
                            "productId must be greater than or equal to 0");
        }
    }

    @Test
    public void testReviewEntityValidationPositive() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            var violations = validator.validate(
                    new ProductEntity(
                            0,
                            "Water",
                            4));

            assertTrue(violations.isEmpty());
        }
    }

    @Test
    public void testProductIdBoundaryValues() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test productId = 0 (minimum valid value)
            var violations = validator.validate(
                    new ProductEntity(
                            0,  // Valid: productId >= 0
                            "Valid Product",
                            1));
            assertTrue(violations.isEmpty());

            // Test productId = -1 (invalid)
            violations = validator.validate(
                    new ProductEntity(
                            -1,  // Invalid: productId < 0
                            "Valid Product",
                            1));
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("productId must be greater than or equal to 0");
        }
    }

    @Test
    public void testProductNameValidation() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test name with exactly 5 characters (minimum valid)
            var violations = validator.validate(
                    new ProductEntity(
                            1,
                            "Tests",  // Exactly 5 characters
                            1));
            assertTrue(violations.isEmpty());

            // Test name with 4 characters (invalid)
            violations = validator.validate(
                    new ProductEntity(
                            1,
                            "Test",  // 4 characters (invalid)
                            1));
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("name size must be between 5 and 100");

            // Test name with exactly 100 characters (maximum valid)
            String name100 = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
            assertEquals(100, name100.length());
            violations = validator.validate(
                    new ProductEntity(
                            1,
                            name100,
                            1));
            assertTrue(violations.isEmpty());

            // Test name with 101 characters (invalid)
            String name101 = name100 + "1";
            violations = validator.validate(
                    new ProductEntity(
                            1,
                            name101,  // 101 characters (invalid)
                            1));
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("name size must be between 5 and 100");

            // Test blank name
            violations = validator.validate(
                    new ProductEntity(
                            1,
                            "",  // Invalid: blank name
                            1));
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("name size must be between 5 and 100");
        }
    }



    @Test
    public void testProductWeightValidation() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test weight = 1 (minimum valid value)
            var violations = validator.validate(
                    new ProductEntity(
                            1,
                            "Valid Product",
                            1));  // Valid: weight >= 1
            assertTrue(violations.isEmpty());

            // Test weight = 0 (invalid)
            violations = validator.validate(
                    new ProductEntity(
                            1,
                            "Valid Product",
                            0));  // Invalid: weight < 1
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("weight must be greater than or equal to 1");

            // Test negative weight
            violations = validator.validate(
                    new ProductEntity(
                            1,
                            "Valid Product",
                            -5));  // Invalid: weight < 1
            assertEquals(1, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .contains("weight must be greater than or equal to 1");
        }
    }

    @Test
    public void testMultipleValidationErrors() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();

            // Test entity with multiple validation errors
            var violations = validator.validate(
                    new ProductEntity(
                            -1,  // Invalid: productId < 0
                            "Bad",  // Invalid: name < 5 characters
                            0));  // Invalid: weight < 1

            assertEquals(3, violations.size());
            assertThat(violations)
                    .extracting(it -> it.getPropertyPath() + " " + it.getMessage())
                    .containsExactlyInAnyOrder(
                            "productId must be greater than or equal to 0",
                            "name size must be between 5 and 100",
                            "weight must be greater than or equal to 1");
        }
    }
}
