package shop.api.core.product;

public record Product(
        int productId,
        String name,
        int weight,
        String serviceAddress
) {
    public Product withServiceAddress(String serviceAddress) {
        return new Product(productId, name, weight, serviceAddress);
    }
}
