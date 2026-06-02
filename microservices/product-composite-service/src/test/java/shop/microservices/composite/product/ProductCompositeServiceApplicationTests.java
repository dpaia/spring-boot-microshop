package shop.microservices.composite.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ProductCompositeServiceApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    public void correctPortIsUsed() {
        assertEquals(
                "7001",
                environment.getProperty("server.port"));
    }
}
