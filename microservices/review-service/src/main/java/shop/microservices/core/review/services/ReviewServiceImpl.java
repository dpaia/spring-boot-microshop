package shop.microservices.core.review.services;

import org.springframework.web.bind.annotation.RestController;
import shop.api.core.review.Review;
import shop.api.core.review.ReviewService;
import shop.api.exceptions.InvalidInputException;

import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 0)
            throw new InvalidInputException("Invalid productId: " + productId);

        return List.of(new Review(0, 0, "", "", "", ""));
    }
}
