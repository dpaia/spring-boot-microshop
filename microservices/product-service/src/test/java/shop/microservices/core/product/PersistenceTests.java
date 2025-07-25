package shop.microservices.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import shop.microservices.core.product.persistence.ProductEntity;
import shop.microservices.core.product.persistence.ProductRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("DataFlowIssue")
@SpringBootTest
class PersistenceTests extends PostgresTestBase {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();

        ProductEntity entity = new ProductEntity(1, "n", 1);
        savedEntity = repository.save(entity).block();

        assertEqualsProduct(entity, savedEntity);
    }

    @Test
    void create() {
        ProductEntity newEntity = new ProductEntity(2, "n", 2);
        repository.save(newEntity).block();

        ProductEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsProduct(newEntity, foundEntity);

        assertEquals(2, repository.count().block());
    }

    @Test
    void update() {
        savedEntity.setName("n2");
        repository.save(savedEntity).block();

        ProductEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(2, (long) foundEntity.getVersion());
        assertEquals("n2", foundEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(savedEntity).block();
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

    @Test
    void getByProductId() {
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId()).blockOptional();

        assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity, entity.get());
    }

    @Test
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
            repository.save(entity).block();
        });
    }

    @Test
    void optimisticLockError() {
        // Store the saved entity in two separate entity objects
        ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setName("n1");
        repository.save(entity1).block();

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e., an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setName("n2");
            repository.save(entity2).block();
        });

        // Get the updated entity from the database and verify its new state
        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(2, (int) updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());
    }

    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getWeight(), actualEntity.getWeight());
    }
}
