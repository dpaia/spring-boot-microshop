package shop.microservices.core.product.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<ProductEntity, Integer> {

    Optional<ProductEntity> findByProductId(int productId);
}
