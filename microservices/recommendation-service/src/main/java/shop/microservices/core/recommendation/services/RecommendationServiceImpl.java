package shop.microservices.core.recommendation.services;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import shop.api.core.recommendation.Recommendation;
import shop.api.core.recommendation.RecommendationService;
import shop.api.exceptions.InvalidInputException;
import shop.microservices.core.recommendation.persistence.RecommendationEntity;
import shop.microservices.core.recommendation.persistence.RecommendationRepository;
import shop.util.http.ServiceUtil;

import java.util.List;

import static shop.microservices.core.recommendation.services.MappingUtils.*;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository repository;

    private final ServiceUtil serviceUtil;

    public RecommendationServiceImpl(RecommendationRepository repository, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            RecommendationEntity entity = apiToEntity(body);
            RecommendationEntity newEntity = repository.save(entity);

            return entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.productId() + ", Recommendation Id:" + body.recommendationId());
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 0) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<RecommendationEntity> entityList = repository.findByProductId(productId);

        return entityListToApiList(entityList)
                .stream()
                .map(r -> r.withServiceAddress(serviceUtil.getServiceAddress()))
                .toList();
    }

    @Override
    public void deleteRecommendations(int productId) {
        repository.deleteAll(repository.findByProductId(productId));
    }
}
