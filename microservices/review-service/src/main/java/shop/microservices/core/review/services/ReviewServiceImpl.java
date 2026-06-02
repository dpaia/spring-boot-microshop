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

@RestController
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;

    private final ServiceUtil serviceUtil;

    private final ReviewMapper reviewMapper;

    @Autowired
    public ReviewServiceImpl(ReviewRepository repository, ServiceUtil serviceUtil, ReviewMapper reviewMapper) {
        this.repository = repository;
        this.serviceUtil = serviceUtil;
        this.reviewMapper = reviewMapper;
    }

    @Override
    public Review createReview(Review body) {
        try {
            ReviewEntity entity = reviewMapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            return reviewMapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.productId() + ", Review Id:" + body.reviewId());
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 0)
            throw new InvalidInputException("Invalid productId: " + productId);

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        return reviewMapper.entityListToApiList(entityList)
                .stream()
                .map(r -> r.withServiceAddress(serviceUtil.getServiceAddress()))
                .toList();
    }

    @Override
    public void deleteReviews(int productId) {
        repository.deleteAll(repository.findByProductId(productId));
    }
}
