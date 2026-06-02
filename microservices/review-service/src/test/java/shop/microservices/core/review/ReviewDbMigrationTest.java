package shop.microservices.core.review;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.clean-disabled: false"
})
public class ReviewDbMigrationTest extends MySqlTestBase {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void testMigration() {
        flyway.clean();
        flyway.migrate();

        BigInteger dbMajorVersion = flyway.info().current().getVersion().getMajor();

        assertThat(dbMajorVersion)
                .isEqualTo(2);

        try {
            //noinspection SqlDialectInspection
            var rows = jdbcClient.sql("select review_id from reviews")
                    .query()
                    .listOfRows();

            assertThat(rows.size()).isEqualTo(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
