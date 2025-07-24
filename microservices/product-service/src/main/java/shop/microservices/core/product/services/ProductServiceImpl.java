package shop.microservices.core.product.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.web.bind.annotation.RestController;
import shop.api.core.product.Product;
import shop.api.core.product.ProductService;
import shop.api.exceptions.InvalidInputException;
import shop.microservices.core.product.persistence.ProductEntity;
import shop.microservices.core.product.persistence.ProductRepository;
import shop.util.http.ServiceUtil;

import static shop.microservices.core.product.services.MapperUtils.apiToEntity;
import static shop.microservices.core.product.services.MapperUtils.entityToApi;

@RestController
public class ProductServiceImpl implements ProductService {

    private final ServiceUtil serviceUtil;

    private final ProductRepository repository;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product createProduct(Product body) {
        try {
            ProductEntity entity = apiToEntity(body);
            ProductEntity newEntity = repository.save(entity);
            return entityToApi(newEntity);

        } catch (DbActionExecutionException e) {
            if (e.getCause() instanceof DuplicateKeyException) {
                throw new InvalidInputException("Duplicate key, Product Id: " + body.productId());
            }
            else {
                throw e;
            }
        }
    }

    @Override
    public Product getProduct(int productId) {
        if (productId < 0) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        var entity = repository.findByProductId(productId);
        if (entity.isEmpty()) {
            throw new InvalidInputException("No product found for productId: " + productId);
        }

        return entityToApi(entity.get())
                .withServiceAddress(serviceUtil.getServiceAddress());
    }

    @Override
    public void deleteProduct(int productId) {
        repository.findByProductId(productId).ifPresent(repository::delete);
    }
}
