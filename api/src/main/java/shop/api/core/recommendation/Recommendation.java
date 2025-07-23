package shop.api.core.recommendation;

public record Recommendation(
        int productId,
        int recommendationId,
        String author,
        int rate,
        String content,
        String serviceAddress
) {
    public Recommendation withServiceAddress(String serviceAddress) {
        return new Recommendation(productId, recommendationId, author, rate, content, serviceAddress);
    }
}
