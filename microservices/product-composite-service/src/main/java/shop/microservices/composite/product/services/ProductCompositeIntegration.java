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

        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    @Override
    public Product createProduct(Product body) {
        try {
            return restTemplate.postForObject(productServiceUrl, body, Product.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;

            return restTemplate.getForObject(url, Product.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            String url = productServiceUrl + "/" + productId;

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            return restTemplate.postForObject(recommendationServiceUrl, body, Recommendation.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;

            return restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
                    })
                    .getBody();

        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl + "?productId=" + productId;
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Review createReview(Review body) {
        try {
            return restTemplate.postForObject(reviewServiceUrl, body, Review.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;

            return restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {
                    })
                    .getBody();

        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteReviews(int productId) {
        try {
            String url = reviewServiceUrl + "?productId=" + productId;

            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        return switch (HttpStatus.resolve(ex.getStatusCode().value())) {
            case NOT_FOUND, UNPROCESSABLE_ENTITY -> new InvalidInputException(getErrorMessage(ex));
            case null, default -> ex;
        };
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ignored) {
            return ex.getMessage();
        }
    }
}
