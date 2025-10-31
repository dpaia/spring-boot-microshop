package shop.api.core.review;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {

    @PostMapping(
            value = "/review",
            consumes = "application/json",
            produces = "application/json")
    Mono<Review> createReview(@Valid @RequestBody Review body);

    @DeleteMapping(value = "/review")
    Mono<Void> deleteReviews(@RequestParam int productId);

    /**
     * Sample usage: "curl $HOST:$PORT/review?productId=1"
     *
     * @param productId ID of the product
     * @return the reviews of the product
     */
    @GetMapping(
            value = "/review",
            produces = "application/json")
    Flux<Review> getReviews(@Valid @RequestParam int productId);
}
