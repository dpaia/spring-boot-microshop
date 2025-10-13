package shop.api.core.review;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Review(
        @Min(0) int productId,
        @Min(0) int reviewId,
        @NotBlank String author,
        @NotBlank String subject,
        @Size(min = 50, max = 200) String content,
        String serviceAddress
) {
    public Review withServiceAddress(String serviceAddress) {
        return new Review(productId, reviewId, author, subject, content, serviceAddress);
    }
}
