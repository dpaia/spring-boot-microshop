package shop.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import shop.api.core.product.Product;
import shop.api.core.product.ProductService;
import shop.api.core.recommendation.Recommendation;
import shop.api.core.recommendation.RecommendationService;
import shop.api.core.review.Review;
import shop.api.core.review.ReviewService;
import shop.api.exceptions.InvalidInputException;
import shop.util.http.HttpErrorInfo;

import java.io.IOException;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @SuppressWarnings("HttpUrlsUsage")
    @Autowired
    public ProductCompositeIntegration(
            ObjectMapper mapper,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort,
            WebClient webClient) {

        this.webClient = webClient;
        this.mapper = mapper;

        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        String url = productServiceUrl;

        return webClient
                .post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Product.class)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = productServiceUrl + "/" + productId;

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        String url = productServiceUrl + "/" + productId;

        return webClient
                .delete()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        String url = recommendationServiceUrl;

        return webClient
                .post()
                .uri(url)
                .retrieve()
                .bodyToMono(Recommendation.class)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;

            return webClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToFlux(Recommendation.class);
        } catch (Exception ex) {
            return Flux.empty();
        }
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        String url = recommendationServiceUrl + "?productId=" + productId;

        return webClient
                .delete()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Review> createReview(Review body) {
        String url = reviewServiceUrl;

        return webClient
                .post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Review.class)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;

            return webClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToFlux(Review.class);

        } catch (Exception ex) {
            return Flux.empty();
        }
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        String url = reviewServiceUrl + "?productId=" + productId;

        return webClient
                .delete()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException wcre)) {
            return ex;
        }

        return switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
            case NOT_FOUND, UNPROCESSABLE_ENTITY -> new InvalidInputException(getErrorMessage(wcre));
            case null, default -> ex;
        };
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException _) {
            return ex.getMessage();
        }
    }
}
