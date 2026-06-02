package shop.microservices.composite.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    public void testSwaggerUi() {
        mockMvcTester.get()
                .uri("/openapi/swagger-ui/index.html")
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK);
    }

    @Test
    public void testOpenApiDocs() {
        mockMvcTester.get()
                .uri("/openapi/v3/api-docs")
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON);
    }
}
