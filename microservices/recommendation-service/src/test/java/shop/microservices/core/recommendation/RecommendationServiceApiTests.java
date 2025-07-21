package shop.microservices.core.recommendation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest
public class RecommendationServiceApiTests {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    public void testProductCompositeServiceApi() {
        mockMvcTester.get()
                .uri("http://localhost:8080/recommendation?productId=0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .hasPath("$[0].productId")
                .hasPath("$[0].recommendationId")
                .hasPath("$[0].author")
                .hasPath("$[0].rate")
                .hasPath("$[0].content")
                .hasPath("$[0].serviceAddress");
    }
}
