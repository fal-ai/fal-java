package ai.fal.client.http;

public class FalException extends RuntimeException {

    public FalException(String message) {
        super(message);
    }

    public FalException(String message, Throwable cause) {
        super(message, cause);
    }

    public FalException(Throwable cause) {
        super(cause);
    }
}
