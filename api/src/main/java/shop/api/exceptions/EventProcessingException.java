package shop.api.exceptions;

public class EventProcessingException extends RuntimeException {

    public EventProcessingException(String message) {
        super(message);
    }
}
