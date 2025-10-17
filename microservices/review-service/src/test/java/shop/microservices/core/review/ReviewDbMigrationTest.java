package shop.microservices.core.review;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest
public class ReviewDbMigrationTest {

    private static final JdbcDatabaseContainer<?> database =
            new MySQLContainer<>("mysql:8.0.32")
                    .withStartupTimeoutSeconds(300)
                    .withDatabaseName("review-db")
                    .withUsername("user")
                    .withPassword("pwd");

    @Autowired
    private DatabaseClient dbClient;

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
        // JDBC configuration for Liquibase
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
        
        // R2DBC configuration for application
        registry.add("spring.r2dbc.url", () -> database.getJdbcUrl().replace("jdbc:", "r2dbc:"));
        registry.add("spring.r2dbc.username", database::getUsername);
        registry.add("spring.r2dbc.password", database::getPassword);
        
        // Liquibase configuration
        registry.add("spring.liquibase.url", database::getJdbcUrl);
        registry.add("spring.liquibase.user", database::getUsername);
        registry.add("spring.liquibase.password", database::getPassword);
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Test
    void testDbMigration() {
        try {
            dbClient.sql("select review_id from reviews")
                    .fetch()
                    .first();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
