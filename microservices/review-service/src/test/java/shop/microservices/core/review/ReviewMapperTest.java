package shop.microservices.core.review;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import shop.api.core.review.Review;
import shop.microservices.core.review.persistence.ReviewEntity;
import shop.microservices.core.review.services.ReviewMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewMapperTest {

    private final ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(mapper);

        Review api = new Review(1, 2, "a", "s", "C", LocalDate.now(), "adr");

        ReviewEntity entity = mapper.apiToEntity(api);

        assertEquals(api.productId(), entity.getProductId());
        assertEquals(api.reviewId(), entity.getReviewId());
        assertEquals(api.author(), entity.getAuthor());
        assertEquals(api.subject(), entity.getSubject());
        assertEquals(api.content(), entity.getContent());
        assertEquals(api.date(), entity.getDate());

        Review api2 = mapper.entityToApi(entity);

        assertEquals(api.productId(), api2.productId());
        assertEquals(api.reviewId(), api2.reviewId());
        assertEquals(api.author(), api2.author());
        assertEquals(api.subject(), api2.subject());
        assertEquals(api.content(), api2.content());
        assertEquals(api.date(), api2.date());
        assertNull(api2.serviceAddress());
    }

    @Test
    void mapperListTests() {
        assertNotNull(mapper);

        Review api = new Review(1, 2, "a", "s", "C", LocalDate.now(), "adr");
        List<Review> apiList = Collections.singletonList(api);

        List<ReviewEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        ReviewEntity entity = entityList.getFirst();

        assertEquals(api.productId(), entity.getProductId());
        assertEquals(api.reviewId(), entity.getReviewId());
        assertEquals(api.author(), entity.getAuthor());
        assertEquals(api.subject(), entity.getSubject());
        assertEquals(api.content(), entity.getContent());
        assertEquals(api.date(), entity.getDate());

        List<Review> api2List = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());

        Review api2 = api2List.getFirst();

        assertEquals(api.productId(), api2.productId());
        assertEquals(api.reviewId(), api2.reviewId());
        assertEquals(api.author(), api2.author());
        assertEquals(api.subject(), api2.subject());
        assertEquals(api.content(), api2.content());
        assertEquals(api.date(), api2.date());
        assertNull(api2.serviceAddress());
    }
}
