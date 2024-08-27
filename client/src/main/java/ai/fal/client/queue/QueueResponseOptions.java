package ai.fal.client.queue;

import ai.fal.client.ApiOptions;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueueResponseOptions<O> implements ApiOptions<JsonNull, O> {

    @Nonnull
    private final String requestId;

    private final Class<O> resultType;

    @Nonnull
    private final JsonNull input = JsonNull.INSTANCE;

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    @Nonnull
    public static QueueResponseOptions<JsonObject> withRequestId(@Nonnull String requestId) {
        return QueueResponseOptions.<JsonObject>builder()
                .requestId(requestId)
                .resultType(JsonObject.class)
                .build();
    }
}
