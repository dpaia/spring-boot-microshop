package shop.microservices.composite.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest
public class ProductCompositeApiTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    public void testProductCompositeServiceApi() {
        mockMvcTester.get()
                .uri("/product-composite/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .hasPath("$.productId")
                .hasPath("$.name")
                .hasPath("$.weight")
                .hasPath("$.recommendations")
                .hasPath("$.reviews")
                .hasPath("$.serviceAddresses");
    }

    @Test
    void testBadRequestForWrongParameterType() {
        mockMvcTester.get()
                .uri("/product-composite/no-integer")
                .accept(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testUnprocessableEntityForNegativeParameter() {
        mockMvcTester.get()
                .uri("/product-composite/-1")
                .accept(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
