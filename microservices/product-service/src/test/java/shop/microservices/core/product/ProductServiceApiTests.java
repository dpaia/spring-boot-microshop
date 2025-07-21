package shop.microservices.core.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest
public class ProductServiceApiTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    public void testProductServiceApi() {
        mockMvcTester.get()
                .uri("http://localhost:8080/product/0")
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
}
