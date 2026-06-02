package shop.api.core.review;

import java.time.LocalDate;

public record Review(
        int productId,
        int reviewId,
        String author,
        String subject,
        String content,
        int rating,
        LocalDate date,
        String serviceAddress
) {
    public Review withServiceAddress(String serviceAddress) {
        return new Review(productId, reviewId, author, subject, content, rating, date, serviceAddress);
    }
}
