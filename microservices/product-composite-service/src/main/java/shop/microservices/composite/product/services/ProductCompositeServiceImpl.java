package shop.microservices.composite.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import shop.api.composite.product.*;
import shop.api.core.product.Product;
import shop.api.core.recommendation.Recommendation;
import shop.api.core.review.Review;
import shop.api.exceptions.InvalidInputException;
import shop.util.http.ServiceUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(
            ServiceUtil serviceUtil,
            ProductCompositeIntegration integration
    ) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public void createProduct(ProductAggregate body) {
        try {
            LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.productId());

            Product product = new Product(body.productId(), body.name(), body.weight(), null);
            integration.createProduct(product);

            if (body.recommendations() != null) {
                body.recommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(
                            body.productId(),
                            r.recommendationId(),
                            r.author(),
                            r.rate(),
                            r.content(),
                            null);
                    integration.createRecommendation(recommendation);
                });
            }

            if (body.reviews() != null) {
                body.reviews().forEach(r -> {
                    Review review = new Review(body.productId(), r.reviewId(), r.author(), r.subject(), r.content(), LocalDate.now(), null);
                    integration.createReview(review);
                });
            }

            LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.productId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeProduct failed", re);
            throw re;
        }
    }


    @Override
    public ProductAggregate getProduct(int productId) {
        LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

        Product product = integration.getProduct(productId);
        if (product == null) {
            throw new InvalidInputException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendations = integration.getRecommendations(productId);

        List<Review> reviews = integration.getReviews(productId);

        LOG.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void deleteProduct(int productId) {
        LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

        integration.deleteProduct(productId);

        integration.deleteRecommendations(productId);

        integration.deleteReviews(productId);

        LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

    private ProductAggregate createProductAggregate(
            Product product,
            List<Recommendation> recommendations,
            List<Review> reviews,
            String serviceAddress) {
        // 1. Setup product info
        int productId = product.productId();
        String name = product.name();
        int weight = product.weight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null)
                        ? null
                        : recommendations.stream()
                        .map(r -> new RecommendationSummary(r.recommendationId(), r.author(), r.rate(), r.content()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries =
                (reviews == null)
                        ? null
                        : reviews.stream()
                        .map(r -> new ReviewSummary(r.reviewId(), r.author(), r.subject(), r.content()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.serviceAddress();
        String reviewAddress = (reviews != null && !reviews.isEmpty())
                ? reviews.getFirst().serviceAddress()
                : "";

        String recommendationAddress = (recommendations != null && !recommendations.isEmpty())
                ? recommendations.getFirst().serviceAddress()
                : "";

        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
