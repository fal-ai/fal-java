package ai.fal.client.queue;

import ai.fal.client.ApiOptions;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueueSubmitOptions<I> implements ApiOptions<I, QueueStatus.InQueue> {

    @Nonnull
    private final I input;

    @Nullable
    private final String webhookUrl;

    @Nonnull
    private final Class<QueueStatus.InQueue> resultType = QueueStatus.InQueue.class;

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    public static <I> QueueSubmitOptions<I> withInput(@Nonnull I input) {
        return QueueSubmitOptions.<I>builder().input(input).build();
    }
}
