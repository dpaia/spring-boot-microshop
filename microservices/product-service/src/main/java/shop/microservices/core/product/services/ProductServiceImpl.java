package shop.microservices.core.product.services;

import org.springframework.web.bind.annotation.RestController;
import shop.api.core.product.Product;
import shop.api.core.product.ProductService;

@RestController
public class ProductServiceImpl implements ProductService {

    @Override
    public Product getProduct(int productId) {
        return new Product(-1, "Something", 1, "");
    }
}
