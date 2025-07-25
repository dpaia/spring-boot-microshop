package shop.microservices.core.review.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import shop.api.core.review.Review;
import shop.api.core.review.ReviewService;
import shop.api.exceptions.InvalidInputException;
import shop.microservices.core.review.persistence.ReviewEntity;
import shop.microservices.core.review.persistence.ReviewRepository;
import shop.util.http.ServiceUtil;

import java.util.List;

import static reactor.core.publisher.Mono.fromCallable;
import static reactor.core.publisher.Mono.fromRunnable;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;

    private final ReviewMapper mapper;

    private final ServiceUtil serviceUtil;

    private final Scheduler jdbcScheduler;

    @Autowired
    public ReviewServiceImpl(
            ReviewRepository repository,
            ReviewMapper mapper,
            ServiceUtil serviceUtil,
            Scheduler jdbcScheduler
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
        this.jdbcScheduler = jdbcScheduler;
    }

    @Override
    public Mono<Review> createReview(Review body) {
        if (body.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.productId());
        }
        return fromCallable(() -> internalCreateReview(body))
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return fromCallable(() -> internalGetReviews(productId))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return fromRunnable(() -> internalDeleteReviews(productId))
                .subscribeOn(jdbcScheduler)
                .then();
    }

    private Review internalCreateReview(Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.productId() + ", Review Id:" + body.reviewId());
        }
    }

    private List<Review> internalGetReviews(int productId) {
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        return mapper.entityListToApiList(entityList)
                .stream()
                .map(e -> e.withServiceAddress(serviceUtil.getServiceAddress()))
                .toList();
    }

    private void internalDeleteReviews(int productId) {
        repository.deleteAll(repository.findByProductId(productId));
    }
}