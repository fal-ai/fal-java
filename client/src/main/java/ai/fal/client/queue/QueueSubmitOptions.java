package ai.fal.client.queue;

import ai.fal.client.ApiOptions;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueueSubmitOptions implements ApiOptions<QueueStatus.InQueue> {

    @Nonnull
    private final Object input;

    @Nullable
    private final String webhookUrl;

    @Nonnull
    private final Class<QueueStatus.InQueue> resultType = QueueStatus.InQueue.class;

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    public static QueueSubmitOptions withInput(@Nonnull Object input) {
        return QueueSubmitOptions.builder().input(input).build();
    }
}
