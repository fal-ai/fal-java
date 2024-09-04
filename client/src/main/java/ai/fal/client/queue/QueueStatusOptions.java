package ai.fal.client.queue;

import ai.fal.client.ApiOptions;
import com.google.gson.JsonNull;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueueStatusOptions implements ApiOptions<QueueStatus> {

    @Nonnull
    private final JsonNull input = JsonNull.INSTANCE;

    @Nonnull
    private final String requestId;

    @Nullable
    private final Boolean logs;

    @Nonnull
    private final Class<QueueStatus> resultType = QueueStatus.class;

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    public static QueueStatusOptions withRequestId(@Nonnull String requestId) {
        return QueueStatusOptions.builder().requestId(requestId).build();
    }
}
