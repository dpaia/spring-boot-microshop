package shop.microservices.core.recommendation.services;

import org.springframework.web.bind.annotation.RestController;
import shop.api.core.recommendation.Recommendation;
import shop.api.core.recommendation.RecommendationService;
import shop.api.exceptions.InvalidInputException;

import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 0)
            throw new InvalidInputException("Invalid productId: " + productId);

        return List.of(new Recommendation(0, 0, "", 0, "", ""));
    }
}
