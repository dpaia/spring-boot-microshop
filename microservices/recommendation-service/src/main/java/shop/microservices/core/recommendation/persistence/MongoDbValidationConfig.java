package shop.microservices.core.recommendation.persistence;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.ReactiveValidatingEntityCallback;

@Configuration
public class MongoDbValidationConfig {

    @Bean
    public ReactiveValidatingEntityCallback validatingEntityCallback(Validator validator) {
        return new ReactiveValidatingEntityCallback(validator);
    }
}
