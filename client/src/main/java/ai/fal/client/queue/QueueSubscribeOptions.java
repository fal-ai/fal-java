package ai.fal.client.queue;

import ai.fal.client.ApiOptions;
import com.google.gson.JsonNull;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueueSubscribeOptions implements ApiOptions<QueueStatus.Completed> {

    private final JsonNull input = JsonNull.INSTANCE;
    private final Class<QueueStatus.Completed> resultType = QueueStatus.Completed.class;

    private final String requestId;
    private final Boolean logs;
    private final Consumer<QueueStatus.StatusUpdate> onQueueUpdate;

    @Override
    public String getHttpMethod() {
        return "GET";
    }
}
