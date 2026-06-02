package shop.microservices.core.product.services;

import shop.api.core.product.Product;
import shop.microservices.core.product.persistence.ProductEntity;

public final class MapperUtils {

    private MapperUtils() {
    }

    public static ProductEntity apiToEntity(Product product) {
        return new ProductEntity(
                product.productId(),
                product.name(),
                product.weight());
    }

    public static Product entityToApi(ProductEntity product) {
        return new Product(
                product.getProductId(),
                product.getName(),
                product.getWeight(),
                null);
    }
}
