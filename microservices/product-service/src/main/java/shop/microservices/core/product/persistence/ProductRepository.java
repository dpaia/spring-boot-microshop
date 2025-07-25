package shop.microservices.core.product.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends R2dbcRepository<ProductEntity, Integer> {

    Mono<ProductEntity> findByProductId(int productId);
}
