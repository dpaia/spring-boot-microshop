package shop.microservices.core.recommendation.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import shop.api.core.recommendation.Recommendation;
import shop.api.core.recommendation.RecommendationService;
import shop.api.exceptions.InvalidInputException;
import shop.microservices.core.recommendation.persistence.RecommendationEntity;
import shop.microservices.core.recommendation.persistence.RecommendationRepository;
import shop.util.http.ServiceUtil;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository repository;

    private final RecommendationMapper mapper;

    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        if (body.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.productId());
        }

        RecommendationEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
                .onErrorMap(
                        DuplicateKeyException.class,
                        _ ->
                                new InvalidInputException(
                                        "Duplicate key, Product Id: " + body.productId() + ", Recommendation Id:" + body.recommendationId()))
                .map(mapper::entityToApi);
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return repository.findByProductId(productId)
                .map(mapper::entityToApi)
                .map(e -> e.withServiceAddress(serviceUtil.getServiceAddress()));
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return repository.deleteAll(repository.findByProductId(productId));
    }
}
