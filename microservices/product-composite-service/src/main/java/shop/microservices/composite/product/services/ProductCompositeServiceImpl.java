package shop.microservices.composite.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import shop.api.composite.product.*;
import shop.api.core.product.Product;
import shop.api.core.recommendation.Recommendation;
import shop.api.core.review.Review;
import shop.util.http.ServiceUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.logging.Level.FINE;

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

    private static void logProductCreateError(String msg) {
        LOG.warn("createCompositeProduct failed: {}", msg);
    }

    @Override
    public Mono<Void> createProduct(ProductAggregate body) {
        try {
            List<Mono<?>> monoList = new ArrayList<>();

            LOG.info("Will create a new composite entity for product.id: {}", body.productId());

            Product product = new Product(body.productId(), body.name(), body.weight(), null);
            monoList.add(integration.createProduct(product));

            if (body.recommendations() != null) {
                body.recommendations().forEach(r -> {
                    Recommendation recommendation = new Recommendation(body.productId(), r.recommendationId(), r.author(), r.rate(), r.content(), null);
                    monoList.add(integration.createRecommendation(recommendation));
                });
            }

            if (body.reviews() != null) {
                body.reviews().forEach(r -> {
                    Review review = new Review(body.productId(), r.reviewId(), r.author(), r.subject(), r.content(), r.rating(), LocalDate.now(), null);
                    monoList.add(integration.createReview(review));
                });
            }

            LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.productId());

            return Mono.zip(_ -> "",
                            monoList.toArray(new Mono[0]))
                    .doOnError(ex -> logProductCreateError(ex.toString()))
                    .then();

        } catch (RuntimeException re) {
            logProductCreateError(re.toString());
            throw re;
        }
    }

    @Override
    public Mono<ProductAggregate> getProduct(int productId) {
        LOG.info("Will get composite product info for product.id={}", productId);
        return Mono.zip(
                        values -> {
                            //noinspection unchecked
                            return createProductAggregate(
                                    (Product) values[0],
                                    (List<Recommendation>) values[1],
                                    (List<Review>) values[2],
                                    serviceUtil.getServiceAddress());
                        },
                        integration.getProduct(productId),
                        integration.getRecommendations(productId).collectList(),
                        integration.getReviews(productId).collectList())
                .doOnError(ex -> LOG.warn("getCompositeProduct failed: {}", ex.toString()))
                .log(LOG.getName(), FINE);
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
                        .map(r -> new ReviewSummary(r.reviewId(), r.author(), r.subject(), r.content(), r.rating()))
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

    @Override
    public Mono<Void> deleteProduct(int productId) {
        try {
            LOG.info("Will delete a product aggregate for product.id: {}", productId);

            return Mono.when(
                            integration.deleteProduct(productId),
                            integration.deleteRecommendations(productId),
                            integration.deleteReviews(productId))
                    .doOnError(ex -> LOG.warn("delete failed: {}", ex.toString()))
                    .log(LOG.getName(), FINE).then();

        } catch (RuntimeException re) {
            LOG.warn("deleteCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }
}
