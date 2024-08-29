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
public class SubscribeOptions<O> implements ApiOptions<Object, O> {

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
    public static <Input> SubscribeOptions<JsonObject> withInput(@Nonnull Input input) {
        return SubscribeOptions.<JsonObject>builder()
                .input(input)
                .resultType(JsonObject.class)
                .build();
    }
}
