package shop.microservices.core.product;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import shop.api.core.product.Product;
import shop.microservices.core.product.persistence.ProductEntity;
import shop.microservices.core.product.services.ProductMapper;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(mapper);

        Product api = new Product(1, "n", 1, "sa");

        ProductEntity entity = mapper.apiToEntity(api);

        assertEquals(api.productId(), entity.getProductId());
        assertEquals(api.name(), entity.getName());
        assertEquals(api.weight(), entity.getWeight());

        Product api2 = mapper.entityToApi(entity);

        assertEquals(api.productId(), api2.productId());
        assertEquals(api.name(), api2.name());
        assertEquals(api.weight(), api2.weight());
        assertNull(api2.serviceAddress());
    }
}
