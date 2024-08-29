package ai.fal.client.queue;

import ai.fal.client.Result;
import jakarta.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public interface AsyncQueueClient {

    @Nonnull
    <I> CompletableFuture<QueueStatus.InQueue> submit(String endpointId, QueueSubmitOptions<I> options);

    @Nonnull
    CompletableFuture<QueueStatus.StatusUpdate> status(@Nonnull String endpointId, @Nonnull QueueStatusOptions options);

    @Nonnull
    CompletableFuture<QueueStatus.Completed> subscribeToStatus(
            @Nonnull String endpointId, @Nonnull QueueSubscribeOptions options);

    @Nonnull
    <O> CompletableFuture<Result<O>> result(@Nonnull String endpointId, @Nonnull QueueResultOptions<O> options);
}
