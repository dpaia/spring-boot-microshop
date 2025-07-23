package shop.microservices.core.review.services;

import shop.api.core.review.Review;
import shop.microservices.core.review.persistence.ReviewEntity;

import java.util.List;

public final class MappingUtils {

    private MappingUtils() {
    }

    public static ReviewEntity apiToEntity(Review review) {
        return new ReviewEntity(
                review.productId(),
                review.reviewId(),
                review.author(),
                review.subject(),
                review.content(),
                review.date()
        );
    }

    public static Review entityToApi(ReviewEntity reviewEntity) {
        return new Review(
                reviewEntity.getProductId(),
                reviewEntity.getReviewId(),
                reviewEntity.getAuthor(),
                reviewEntity.getSubject(),
                reviewEntity.getContent(),
                reviewEntity.getDate(),
                null
        );
    }

    public static List<Review> entityListToApiList(List<ReviewEntity> reviewEntityList) {
        return reviewEntityList.stream()
                .map(MappingUtils::entityToApi)
                .toList();
    }
}
