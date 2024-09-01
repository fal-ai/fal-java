package ai.fal.client.queue;

import ai.fal.client.ApiOptions;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueueResultOptions<O> implements ApiOptions<O> {

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
    public static QueueResultOptions<JsonObject> withRequestId(@Nonnull String requestId) {
        return QueueResultOptions.<JsonObject>builder()
                .requestId(requestId)
                .resultType(JsonObject.class)
                .build();
    }
}
