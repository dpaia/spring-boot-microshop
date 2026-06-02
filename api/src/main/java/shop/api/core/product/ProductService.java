package shop.api.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<Product> createProduct(Product body);

    Mono<Void> deleteProduct(int productId);

    /**
     * Sample usage: "curl $HOST:$PORT/product/1".
     *
     * @param productId ID of the product
     * @return the product, if found, else null
     */
    @GetMapping(
            value = "/product/{productId}",
            produces = "application/json")
    Mono<Product> getProduct(@PathVariable("productId") int productId);

    @GetMapping(
            value = "/product",
            produces = "application/json")
    Flux<Product> getAllProducts();
}
