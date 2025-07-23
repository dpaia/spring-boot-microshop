package shop.microservices.core.recommendation.services;

import shop.api.core.recommendation.Recommendation;
import shop.microservices.core.recommendation.persistence.RecommendationEntity;

import java.util.List;

public final class MappingUtils {

    private MappingUtils() {
    }

    public static RecommendationEntity apiToEntity(Recommendation recommendation) {
        return new RecommendationEntity(
                recommendation.productId(),
                recommendation.recommendationId(),
                recommendation.author(),
                recommendation.rate(),
                recommendation.content());
    }

    public static Recommendation entityToApi(RecommendationEntity recommendation) {
        return new Recommendation(
                recommendation.getProductId(),
                recommendation.getRecommendationId(),
                recommendation.getAuthor(),
                recommendation.getRating(),
                recommendation.getContent(),
                null);
    }

    public static List<Recommendation> entityListToApiList(List<RecommendationEntity> recommendationEntityList) {
        return recommendationEntityList.stream()
                .map(MappingUtils::entityToApi)
                .toList();
    }
}
