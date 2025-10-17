package shop.microservices.core.review.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface ReviewRepository extends R2dbcRepository<ReviewEntity, Integer> {

    Flux<ReviewEntity> findByProductId(int productId);
}
