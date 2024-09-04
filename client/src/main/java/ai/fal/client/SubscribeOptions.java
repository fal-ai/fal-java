package ai.fal.client;

import ai.fal.client.queue.QueueStatus;
import com.google.gson.JsonObject;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscribeOptions<O> implements ApiOptions<O> {

    @Nonnull
    private final Object input;

    @Nullable
    private final String webhookUrl;

    @Nonnull
    private final Class<O> resultType;

    @Nullable
    private final Boolean logs;

    @Nullable
    private final Consumer<QueueStatus.StatusUpdate> onUpdate;

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    @Nonnull
    public static SubscribeOptions<JsonObject> withInput(@Nonnull Object input) {
        return withInput(input, JsonObject.class);
    }

    @Nonnull
    public static <O> SubscribeOptions<O> withInput(@Nonnull Object input, @Nullable Class<O> resultType) {
        return SubscribeOptions.<O>builder().input(input).resultType(resultType).build();
    }
}
