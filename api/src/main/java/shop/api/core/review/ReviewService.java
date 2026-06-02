package shop.api.core.review;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {

    Mono<Review> createReview(Review body);

    Mono<Void> deleteReviews(int productId);

    /**
     * Sample usage: "curl $HOST:$PORT/review?productId=1".
     *
     * @param productId ID of the product
     * @return the reviews of the product
     */
    @GetMapping(
            value = "/review",
            produces = "application/json")
    Flux<Review> getReviews(@RequestParam int productId);
}
