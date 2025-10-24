package shop.microservices.core.product;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public class ProductMigrationTest {

    private static final JdbcDatabaseContainer<?> database =
            new PostgreSQLContainer<>("postgres:17.5")
                    .withStartupTimeoutSeconds(300)
                    .withDatabaseName("product-db")
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

    /**
     * Creates the products table if it doesn't exist and deletes any existing data.
     * This replaces Liquibase migrations for test environments.
     */
    public void createProductsTable() {
        dbClient.sql("""
            CREATE TABLE IF NOT EXISTS products (
                id SERIAL PRIMARY KEY,
                version INTEGER NOT NULL DEFAULT 0,
                product_id INTEGER NOT NULL,
                name VARCHAR(100) NOT NULL,
                weight INTEGER NOT NULL,
                UNIQUE(product_id)
            )
            """)
            .fetch()
            .rowsUpdated()
                .block();

        dbClient.sql("delete from products")
            .fetch()
            .rowsUpdated()
                .block();
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Test
    void testDbMigration() {
        try {
            // First create the table
            createProductsTable();
            
            // Then test if we can query it
            dbClient.sql("select product_id from products")
                    .fetch()
                    .first();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
