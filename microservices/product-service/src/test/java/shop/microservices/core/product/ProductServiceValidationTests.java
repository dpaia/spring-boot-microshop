package shop.microservices.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import shop.api.core.product.Product;
import shop.api.core.product.ProductService;
import shop.api.exceptions.InvalidInputException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { 
    "eureka.client.enabled=false",
    "spring.liquibase.enabled=false",
    "spring.rabbitmq.host=localhost",
    "spring.r2dbc.pool.enabled=false"
})
public class ProductServiceValidationTests extends ProductMigrationTest {

    @Autowired
    private ProductService productService;

    @Test
    public void testCreateProductWithValidData() {
        Product validProduct = new Product(
                0,  // productId >= 0 ✓
                "Valid Product Name",  // name not blank ✓
                10,  // weight > 0 ✓
                "serviceAddress"
        );

        // This should not throw any exception
        productService.createProduct(validProduct).block();
    }

    @Test
    public void testCreateProductWithNegativeProductId() {
        Product invalidProduct = new Product(
                -1,  // Invalid: productId < 0
                "Valid Product Name",
                10,
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> productService.createProduct(invalidProduct).block()
        );
        assertTrue(exception.getMessage().contains("Invalid productId: -1"));
    }

    @Test
    public void testCreateProductWithBlankName() {
        Product invalidProduct = new Product(
                1,
                "",  // Invalid: name is blank
                10,
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> productService.createProduct(invalidProduct).block()
        );
        assertTrue(exception.getMessage().contains("must not be blank") || 
                   exception.getMessage().contains("size must be between"));
    }

    @Test
    public void testCreateProductWithShortName() {
        Product invalidProduct = new Product(
                1,
                "Test",  // Invalid: name < 5 characters
                10,
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> productService.createProduct(invalidProduct).block()
        );
        assertTrue(exception.getMessage().contains("size must be between"));
    }

    @Test
    public void testCreateProductWithExactlyFiveCharacterName() {
        Product validProduct = new Product(
                1,
                "Tests",  // Exactly 5 characters (minimum valid)
                10,
                "serviceAddress"
        );

        // This should not throw any exception
        productService.createProduct(validProduct).block();
    }

    @Test
    public void testCreateProductWithInvalidWeight() {
        Product invalidProduct = new Product(
                1,
                "Valid Product Name",
                0,  // Invalid: weight <= 0
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> productService.createProduct(invalidProduct).block()
        );
        assertTrue(exception.getMessage().contains("must be greater than") || 
                   exception.getMessage().contains("weight"));
    }

    @Test
    public void testCreateProductWithNegativeWeight() {
        Product invalidProduct = new Product(
                1,
                "Valid Product Name",
                -5,  // Invalid: weight < 0
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> productService.createProduct(invalidProduct).block()
        );
        assertTrue(exception.getMessage().contains("must be greater than") || 
                   exception.getMessage().contains("weight"));
    }

    @Test
    public void testCreateProductWithMultipleValidationErrors() {
        Product invalidProduct = new Product(
                -1,  // Invalid: productId < 0
                "Bad",  // Invalid: name < 5 characters
                0,  // Invalid: weight <= 0
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> productService.createProduct(invalidProduct).block()
        );

        // Should contain at least one validation error message
        String message = exception.getMessage();
        assertTrue(message.contains("Invalid productId") || 
                   message.contains("size must be between") || 
                   message.contains("must be greater than"));
    }

    @Test
    public void testCreateProductWithLongValidName() {
        // Test with maximum valid name length (100 characters)
        String longName = "This is a very long product name that contains exactly one hundred characters for test 123";
        
        Product validProduct = new Product(
                1,
                longName,  // Should be valid if <= 100 characters
                10,
                "serviceAddress"
        );

        // This should not throw any exception
        productService.createProduct(validProduct).block();
    }

    @Test
    public void testCreateProductWithTooLongName() {
        // Test with name exceeding maximum length (101 characters)
        String tooLongName = "This is a very long product name that contains exactly one hundred and one characters for testing purposes ";
        
        Product invalidProduct = new Product(
                1,
                tooLongName,  // Should be invalid if > 100 characters
                10,
                "serviceAddress"
        );

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> productService.createProduct(invalidProduct).block()
        );
        assertTrue(exception.getMessage().contains("size must be between") || 
                   exception.getMessage().contains("too long"));
    }
}