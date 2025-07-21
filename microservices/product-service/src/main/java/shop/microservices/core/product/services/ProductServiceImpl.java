package shop.microservices.core.product.services;

import org.springframework.web.bind.annotation.RestController;
import shop.api.core.product.Product;
import shop.api.core.product.ProductService;
import shop.api.exceptions.InvalidInputException;

@RestController
public class ProductServiceImpl implements ProductService {

    @Override
    public Product getProduct(int productId) {
        if (productId < 0)
            throw new InvalidInputException("Invalid productId: " + productId);

        return new Product(0, "Something", 1, "");
    }
}
