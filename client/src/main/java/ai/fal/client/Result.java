package ai.fal.client;

import jakarta.annotation.Nonnull;

public class Result<T> {
    private final T data;
    private final String requestId;

    public Result(T data, String requestId) {
        this.data = data;
        this.requestId = requestId;
    }

    public T getData() {
        return data;
    }

    @Nonnull
    public String getRequestId() {
        return requestId;
    }
}
