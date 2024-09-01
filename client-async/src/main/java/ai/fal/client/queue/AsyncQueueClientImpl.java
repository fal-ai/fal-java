package ai.fal.client.queue;

import ai.fal.client.Output;
import ai.fal.client.exception.FalException;
import ai.fal.client.http.HttpClient;
import ai.fal.client.util.EndpointId;
import com.google.gson.JsonObject;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

public class AsyncQueueClientImpl implements AsyncQueueClient {

    private final HttpClient httpClient;

    public AsyncQueueClientImpl(@Nonnull HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Nonnull
    @Override
    public CompletableFuture<QueueStatus.InQueue> submit(String endpointId, QueueSubmitOptions options) {
        final var url = "https://queue.fal.run/" + endpointId;
        final var queryParams = new HashMap<String, Object>();
        if (options.getWebhookUrl() != null) {
            queryParams.put("fal_webhook", options.getWebhookUrl());
        }
        final var request = httpClient.prepareRequest(url, options, queryParams);
        return httpClient
                .executeRequestAsync(request)
                .thenApply(response -> httpClient.handleResponse(response, QueueStatus.InQueue.class));
    }

    @Nonnull
    @Override
    public CompletableFuture<QueueStatus.StatusUpdate> status(
            @Nonnull String endpointId, @Nonnull QueueStatusOptions options) {
        final var endpoint = EndpointId.fromString(endpointId);
        final var url = String.format(
                "https://queue.fal.run/%s/%s/requests/%s/status",
                endpoint.getAppOwner(), endpoint.getAppName(), options.getRequestId());

        final var queryParams = new HashMap<String, Object>();
        if (options.getLogs() != null && options.getLogs()) {
            queryParams.put("logs", "1");
        }

        final var request = httpClient.prepareRequest(url, options, queryParams);
        return httpClient.executeRequestAsync(request).thenApply((response) -> {
            final var result = httpClient.handleResponse(response, JsonObject.class);
            return httpClient.fromJson(result, QueueStatus.resolveType(result));
        });
    }

    @Nonnull
    @Override
    public CompletableFuture<QueueStatus.Completed> subscribeToStatus(
            @Nonnull String endpointId, @Nonnull QueueSubscribeOptions options) {
        final var endpoint = EndpointId.fromString(endpointId);
        final var url = String.format(
                "https://queue.fal.run/%s/%s/requests/%s/status/stream",
                endpoint.getAppOwner(), endpoint.getAppName(), options.getRequestId());

        final var queryParams = new HashMap<String, Object>();
        if (options.getLogs() != null && options.getLogs()) {
            queryParams.put("logs", "1");
        }
        final var request = httpClient
                .prepareRequest(url, options, queryParams)
                .newBuilder()
                .addHeader("Accept", "text/event-stream")
                .build();

        final var future = new CompletableFuture<QueueStatus.Completed>();

        final var factory = EventSources.createFactory(httpClient.getUnderlyingClient());
        final var listener = new EventSourceListener() {
            private QueueStatus.StatusUpdate currentStatus;

            @Override
            public void onEvent(
                    @Nonnull EventSource eventSource,
                    @Nullable String id,
                    @Nullable String type,
                    @Nonnull String data) {
                final var json = httpClient.fromJson(data, JsonObject.class);
                final var status = httpClient.fromJson(json, QueueStatus.resolveType(json));
                final var onUpdate = options.getOnUpdate();
                if (onUpdate != null) {
                    onUpdate.accept(status);
                }
                this.currentStatus = status;
            }

            @Override
            public void onClosed(@Nonnull EventSource eventSource) {
                if (currentStatus != null && currentStatus instanceof QueueStatus.Completed) {
                    future.complete((QueueStatus.Completed) currentStatus);
                    return;
                }
                future.completeExceptionally(new FalException(
                        "Streaming closed with invalid state: " + currentStatus, options.getRequestId()));
            }

            @Override
            public void onFailure(
                    @Nonnull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                future.completeExceptionally(t);
            }
        };
        factory.newEventSource(request, listener);
        return future;
    }

    @Nonnull
    @Override
    public <O> CompletableFuture<Output<O>> result(@Nonnull String endpointId, @Nonnull QueueResultOptions<O> options) {
        final var endpoint = EndpointId.fromString(endpointId);
        final var url = String.format(
                "https://queue.fal.run/%s/%s/requests/%s",
                endpoint.getAppOwner(), endpoint.getAppName(), options.getRequestId());
        final var request = httpClient.prepareRequest(url, options);

        return httpClient
                .executeRequestAsync(request)
                .thenApply((response) -> httpClient.wrapInResult(response, options.getResultType()));
    }
}
