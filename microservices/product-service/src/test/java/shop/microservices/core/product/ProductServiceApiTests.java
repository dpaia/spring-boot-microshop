package shop.microservices.core.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest
public class ProductServiceApiTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    public void testProductServiceApi() {
        mockMvcTester.get()
                .uri("/product/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .hasPath("$.productId")
                .hasPath("$.name")
                .hasPath("$.weight")
                .hasPath("$.serviceAddress");
    }

    @Test
    void testBadRequestForWrongParameterType() {
        mockMvcTester.get()
                .uri("/product/no-integer")
                .accept(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testUnprocessableEntityForNegativeParameter() {
        mockMvcTester.get()
                .uri("/product/-1")
                .accept(APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
