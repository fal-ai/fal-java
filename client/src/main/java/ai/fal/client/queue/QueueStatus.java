package ai.fal.client.queue;

import ai.fal.client.type.RequestLog;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

public interface QueueStatus {

    enum Status {
        IN_QUEUE,
        IN_PROGRESS,
        COMPLETED,
    }

    interface StatusUpdate {
        @Nonnull
        Status getStatus();

        @Nonnull
        String getRequestId();

        @Nonnull
        String getStatusUrl();

        @Nonnull
        String getResponseUrl();

        @Nonnull
        String getCancelUrl();
    }

    @Data
    @NoArgsConstructor
    class BaseStatusUpdate implements StatusUpdate {

        @Nonnull
        protected Status status;

        @Nonnull
        @SerializedName("request_id")
        protected String requestId;

        @Nonnull
        @SerializedName("status_url")
        protected String statusUrl;

        @Nonnull
        @SerializedName("response_url")
        protected String responseUrl;

        @Nonnull
        @SerializedName("cancel_url")
        protected String cancelUrl;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    class InQueue extends BaseStatusUpdate {

        @Nonnull
        @SerializedName("queue_position")
        private Integer queuePosition;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    class InProgress extends BaseStatusUpdate {

        @Nullable
        @SerializedName("logs")
        private List<RequestLog> logs;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    class Completed extends BaseStatusUpdate {

        @Nullable
        @SerializedName("logs")
        private List<RequestLog> logs;
    }

    static Class<? extends StatusUpdate> resolveType(JsonObject payload) {
        final var status = payload.get("status").getAsString();
        if (status.equals(QueueStatus.Status.IN_QUEUE.name())) {
            return InQueue.class;
        }
        if (status.equals(QueueStatus.Status.IN_PROGRESS.name())) {
            return QueueStatus.InProgress.class;
        }
        if (status.equals(QueueStatus.Status.COMPLETED.name())) {
            return QueueStatus.Completed.class;
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }
}
