package shop.api.composite.product;

public record ReviewSummary(
        int reviewId,
        String author,
        String subject,
        String content
) {
}
