package shop.microservices.core.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import shop.microservices.core.review.persistence.ReviewEntity;
import shop.microservices.core.review.persistence.ReviewRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("DataFlowIssue")
@SpringBootTest
class PersistenceTests extends MySqlTestBase {

    private static final String REVIEW_CONTENT = "Lorem ipsum dolor sit amet, consetetur sadipscingw";

    @Autowired
    private ReviewRepository repository;

    private ReviewEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();

        ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", REVIEW_CONTENT, 4, LocalDate.now());
        savedEntity = repository.save(entity).block();

        assertEqualsReview(entity, savedEntity);
    }

    @Test
    void create() {
        ReviewEntity newEntity = new ReviewEntity(1, 3, "a", "s", REVIEW_CONTENT, 4, LocalDate.now());
        repository.save(newEntity).block();

        ReviewEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsReview(newEntity, foundEntity);

        assertEquals(2, repository.count().block());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity).block();

        ReviewEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(2, (long) foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedEntity).block();
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

    @Test
    void getByProductId() {
        List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId()).collectList().block();

        assertEquals(1, entityList.size());
        assertEqualsReview(savedEntity, entityList.getFirst());
    }

    @Test
    void duplicateError() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", REVIEW_CONTENT, 4, LocalDate.now());
            repository.save(entity).block();
        });
    }

    @Test
    void optimisticLockError() {
        // Store the saved entity in two separate entity objects
        ReviewEntity entity1 = repository.findById(savedEntity.getId()).block();
        ReviewEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setAuthor("a1");
        repository.save(entity1).block();

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e., an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setAuthor("a2");
            repository.save(entity2).block();
        });

        // Get the updated entity from the database and verify its new state
        ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(2, (int) updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
        assertEquals(expectedEntity.getDate(), actualEntity.getDate());
    }
}