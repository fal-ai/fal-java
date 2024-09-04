package ai.fal.client.exception;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class FalException extends RuntimeException {

    @Nullable
    private final String requestId;

    public FalException(@Nonnull String message, @Nullable String requestId) {
        super(requireNonNull(message));
        this.requestId = requestId;
    }

    public FalException(@Nonnull String message, @Nonnull Throwable cause, @Nullable String requestId) {
        super(requireNonNull(message), cause);
        this.requestId = requestId;
    }

    public FalException(Throwable cause) {
        super(cause);
        this.requestId = null;
    }

    @Nullable
    public String getRequestId() {
        return this.requestId;
    }
}
