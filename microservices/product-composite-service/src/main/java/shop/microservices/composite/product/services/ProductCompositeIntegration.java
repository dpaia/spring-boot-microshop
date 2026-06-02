package shop.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import shop.api.core.product.Product;
import shop.api.core.product.ProductService;
import shop.api.core.recommendation.Recommendation;
import shop.api.core.recommendation.RecommendationService;
import shop.api.core.review.Review;
import shop.api.core.review.ReviewService;
import shop.api.exceptions.InvalidInputException;
import shop.util.http.HttpErrorInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @SuppressWarnings("HttpUrlsUsage")
    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort) {

        this.restTemplate = restTemplate;
        this.mapper = mapper;

        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
    }

    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + productId;

            Product product = restTemplate.getForObject(url, Product.class);
            if (product == null) {
                throw new InvalidInputException("No product found for productId: " + productId);
            }

            return product;

        } catch (HttpClientErrorException ex) {
            switch (HttpStatus.resolve(ex.getStatusCode().value())) {
                case NOT_FOUND, UNPROCESSABLE_ENTITY:
                    throw new InvalidInputException(getErrorMessage(ex));

                case null:
                default:
                    throw ex;
            }
        }
    }

    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + productId;

            List<Recommendation> recommendations = restTemplate.exchange(url, GET, null,
                            new ParameterizedTypeReference<List<Recommendation>>() {
                            })
                    .getBody();

            if (recommendations == null) {
                recommendations = new ArrayList<>();
            }

            return recommendations;

        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;

            List<Review> reviews = restTemplate.exchange(url, GET, null,
                            new ParameterizedTypeReference<List<Review>>() {
                            })
                    .getBody();
            if (reviews == null) {
                reviews = new ArrayList<>();
            }

            return reviews;

        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    @Override
    public Review createReview(Review body) {
        return restTemplate.postForObject(reviewServiceUrl, body, Review.class);
    }

    @Override
    public void deleteReviews(int productId) {
        String url = reviewServiceUrl + "?productId=" + productId;
        restTemplate.delete(url);
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ignored) {
            return ex.getMessage();
        }
    }
}
