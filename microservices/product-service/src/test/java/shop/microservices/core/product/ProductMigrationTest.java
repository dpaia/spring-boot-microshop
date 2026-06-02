package shop.microservices.core.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;

@SpringBootTest
public class ProductMigrationTest extends PostgresTestBase {

    @Autowired
    private DatabaseClient dbClient;

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Test
    void testDbMigration() {
        try {
            dbClient.sql("select product_id from products")
                    .fetch()
                    .first();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
