package shop.microservices.core.product;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgresTestBase {

    // Extend startup timeout since a MySQLContainer with MySQL 8 starts very slow on Win10/WSL2
    private static final JdbcDatabaseContainer<?> database =
            new PostgreSQLContainer<>("postgres:latest")
                    .withStartupTimeoutSeconds(300)
                    .withDatabaseName("product-db")
                    .withUsername("user")
                    .withPassword("pwd");

    @BeforeAll
    public static void startDb() {
        database.start();
    }

    @AfterAll
    public static void stopDb() {
        database.stop();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }
}
