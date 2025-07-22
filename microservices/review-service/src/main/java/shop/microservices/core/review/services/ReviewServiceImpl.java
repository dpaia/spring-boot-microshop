package shop.microservices.core.review.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import shop.api.core.review.Review;
import shop.api.core.review.ReviewService;
import shop.api.exceptions.InvalidInputException;
import shop.microservices.core.review.persistence.ReviewEntity;
import shop.microservices.core.review.persistence.ReviewRepository;
import shop.util.http.ServiceUtil;

import java.util.List;

import static shop.microservices.core.review.services.MappingUtils.*;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;

    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(ReviewRepository repository, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Review createReview(Review body) {
        try {
            ReviewEntity entity = apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            return entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.productId() + ", Review Id:" + body.reviewId());
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 0)
            throw new InvalidInputException("Invalid productId: " + productId);

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        return entityListToApiList(entityList)
                .stream()
                .map(r -> r.withServiceAddress(serviceUtil.getServiceAddress()))
                .toList();
    }

    @Override
    public void deleteReviews(int productId) {
        repository.deleteAll(repository.findByProductId(productId));
    }
}
