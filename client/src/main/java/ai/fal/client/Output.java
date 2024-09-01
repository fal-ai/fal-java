package ai.fal.client;

import jakarta.annotation.Nonnull;
import java.util.Objects;

/**
 * Represents the output of a request. It contains the data and the {@code requestId}.
 * @param <T> the type of the data in the output
 */
public class Output<T> {

    private final T data;

    private final String requestId;

    public Output(@Nonnull T data, @Nonnull String requestId) {
        this.data = Objects.requireNonNull(data);
        this.requestId = Objects.requireNonNull(requestId);
    }

    @Nonnull
    public T getData() {
        return data;
    }

    @Nonnull
    public String getRequestId() {
        return requestId;
    }
}
