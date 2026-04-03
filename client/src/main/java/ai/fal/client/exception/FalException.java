package ai.fal.client.exception;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class FalException extends RuntimeException {

    @Nullable
    private final String requestId;

    private final int statusCode;

    @Nullable
    private final String body;

    public FalException(@Nonnull String message, @Nullable String requestId) {
        super(requireNonNull(message));
        this.requestId = requestId;
        this.statusCode = -1;
        this.body = null;
    }

    public FalException(
            @Nonnull String message,
            @Nullable String requestId,
            int statusCode,
            @Nullable String body) {
        super(requireNonNull(message));
        this.requestId = requestId;
        this.statusCode = statusCode;
        this.body = body;
    }

    public FalException(@Nonnull String message, @Nonnull Throwable cause, @Nullable String requestId) {
        super(requireNonNull(message), cause);
        this.requestId = requestId;
        this.statusCode = -1;
        this.body = null;
    }

    public FalException(Throwable cause) {
        super(cause);
        this.requestId = null;
        this.statusCode = -1;
        this.body = null;
    }

    @Nullable
    public String getRequestId() {
        return this.requestId;
    }

    /**
     * Returns the HTTP status code of the failed response, or -1 if not available.
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Returns the raw response body of the failed response, or null if not available.
     */
    @Nullable
    public String getBody() {
        return this.body;
    }
}
