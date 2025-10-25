package shop.microservices.core.review.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import shop.api.core.review.Review;
import shop.api.core.review.ReviewService;
import shop.api.exceptions.InvalidInputException;
import shop.microservices.core.review.persistence.ReviewEntity;
import shop.microservices.core.review.persistence.ReviewRepository;
import shop.util.http.ServiceUtil;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;

    private final ReviewMapper mapper;

    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(
            ReviewRepository repository,
            ReviewMapper mapper,
            ServiceUtil serviceUtil
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Review> createReview(Review body) {
        if (body.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.productId());
        }

        ReviewEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
                .onErrorMap(
                        DataIntegrityViolationException.class,
                        _ -> new InvalidInputException("Duplicate key, Product Id: " + body.productId() + ", Review Id:" + body.reviewId()))
                .map(mapper::entityToApi);
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return repository.findByProductId(productId)
                .map(mapper::entityToApi)
                .map(e -> e.withServiceAddress(serviceUtil.getServiceAddress()));
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return repository.findByProductId(productId)
                .flatMap(repository::delete)
                .then();
    }
}