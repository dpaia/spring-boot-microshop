package shop.api.core.review;

import java.time.LocalDate;

public record Review(
        int productId,
        int reviewId,
        String author,
        String subject,
        String content,
        LocalDate date,
        String serviceAddress
) {
    public Review withServiceAddress(String serviceAddress) {
        return new Review(productId, reviewId, author, subject, content, date, serviceAddress);
    }
}
