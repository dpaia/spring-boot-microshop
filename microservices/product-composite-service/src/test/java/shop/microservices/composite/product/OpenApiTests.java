package shop.microservices.composite.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testSwaggerUi() {
        webTestClient.get()
                .uri("/openapi/swagger-ui/index.html")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testOpenApiDocs() {
        webTestClient.get()
                .uri("/openapi/v3/api-docs")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }
}
