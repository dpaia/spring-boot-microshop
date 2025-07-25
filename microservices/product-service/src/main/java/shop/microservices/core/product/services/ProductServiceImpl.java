package shop.microservices.core.product.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import shop.api.core.product.Product;
import shop.api.core.product.ProductService;
import shop.api.exceptions.InvalidInputException;
import shop.api.exceptions.NotFoundException;
import shop.microservices.core.product.persistence.ProductEntity;
import shop.microservices.core.product.persistence.ProductRepository;
import shop.util.http.ServiceUtil;

@RestController
public class ProductServiceImpl implements ProductService {

    private final ServiceUtil serviceUtil;

    private final ProductRepository repository;

    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        if (body.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.productId());
        }

        ProductEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
                .onErrorMap(
                        DuplicateKeyException.class,
                        _ ->
                                new InvalidInputException(
                                        "Duplicate key, Product Id: " + body.productId()))
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .map(mapper::entityToApi)
                .map(e -> e.withServiceAddress(serviceUtil.getServiceAddress()));
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return repository.findByProductId(productId)
                .map(repository::delete)
                .flatMap(e -> e);
    }
}