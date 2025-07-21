package shop.util.http;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public record HttpErrorInfo(HttpStatus httpStatus,
                            String path,
                            String message,
                            ZonedDateTime timestamp
) {

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
        this(httpStatus, path, message, ZonedDateTime.now());
    }
}
