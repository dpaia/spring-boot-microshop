package shop.microservices.composite.product.services;

import org.springframework.web.bind.annotation.RestController;
import shop.api.composite.product.ProductAggregate;
import shop.api.composite.product.ProductCompositeService;
import shop.api.composite.product.ServiceAddresses;
import shop.api.exceptions.InvalidInputException;

import java.util.List;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    @Override
    public ProductAggregate getProduct(int productId) {
        if (productId < 0)
            throw new InvalidInputException("Invalid productId: " + productId);

        return new ProductAggregate(-1, "Something", 1, List.of(), List.of(), new ServiceAddresses("", "", "", ""));
    }
}
