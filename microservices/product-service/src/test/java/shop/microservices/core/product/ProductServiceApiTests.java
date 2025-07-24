package shop.microservices.core.product;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.json.AbstractJsonContentAssert;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import shop.api.core.product.Product;
import shop.microservices.core.product.persistence.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc
@SpringBootTest
public class ProductServiceApiTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    void getProductById() {
        int productId = 1;

        postAndVerifyProduct(productId, OK);

        assertTrue(repository.findByProductId(productId).isPresent());

        getAndVerifyProduct(productId, OK)
                .hasPathSatisfying("$.productId", it -> it.assertThat().isEqualTo(productId));
    }

    @Test
    void duplicateError() {
        int productId = 1;

        postAndVerifyProduct(productId, OK);

        assertTrue(repository.findByProductId(productId).isPresent());

        postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
                .hasPathSatisfying("$.path", it -> it.assertThat().isEqualTo("/product"))
                .hasPathSatisfying("$.message", it -> it.assertThat().isEqualTo("Duplicate key, Product Id: " + productId));
    }

    @Test
    void deleteProduct() {
        int productId = 1;

        postAndVerifyProduct(productId, OK);
        assertTrue(repository.findByProductId(productId).isPresent());

        deleteAndVerifyProduct(productId, OK);
        assertFalse(repository.findByProductId(productId).isPresent());

        deleteAndVerifyProduct(productId, OK);
    }

    @Test
    void getProductInvalidParameterString() {
        getAndVerifyProduct("/no-integer", BAD_REQUEST);
    }

    @Test
    void getProductInvalidParameterNegativeValue() {
        int productIdInvalid = -1;

        getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
                .hasPathSatisfying("$.path", it -> it.assertThat().isEqualTo("/product/" + productIdInvalid))
                .hasPathSatisfying("$.message", it -> it.assertThat().isEqualTo("Invalid productId: " + productIdInvalid));
    }

    private AbstractJsonContentAssert<?> getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return getAndVerifyProduct("/" + productId, expectedStatus);
    }

    private AbstractJsonContentAssert<?> getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
        return mockMvcTester.get()
                .uri("/product" + productIdPath)
                .contentType(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(expectedStatus)
                .bodyJson();
    }

    private AbstractJsonContentAssert<?> postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        Product product = new Product(productId, "Name " + productId, productId, "SA");
        return mockMvcTester.post()
                .uri("/product")
                .contentType(APPLICATION_JSON)
                .content(new Gson().toJson(product))
                .exchange()
                .assertThat()
                .hasStatus(expectedStatus)
                .bodyJson();
    }

    @SuppressWarnings("SameParameterValue")
    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        mockMvcTester.delete()
                .uri("/product/" + productId)
                .contentType(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(expectedStatus);
    }
}
