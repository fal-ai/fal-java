package ai.fal.client.queue;

import ai.fal.client.Result;
import ai.fal.client.http.FalException;
import ai.fal.client.http.HttpClient;
import ai.fal.client.queue.QueueStatus.Completed;
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

public class QueueClientImpl implements QueueClient {

    private final HttpClient httpClient;

    public QueueClientImpl(@Nonnull HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Nonnull
    @Override
    public <I> QueueStatus.InQueue submit(@Nonnull String endpointId, @Nonnull QueueSubmitOptions<I> options) {
        final var url = "https://queue.fal.run/" + endpointId;
        final var queryParams = new HashMap<String, Object>();
        if (options.getWebhookUrl() != null) {
            queryParams.put("fal_webhook", options.getWebhookUrl());
        }
        final var request = httpClient.prepareRequest(url, options, queryParams);
        final var response = httpClient.executeRequest(request);
        return httpClient.handleResponse(response, QueueStatus.InQueue.class);
    }

    @Nonnull
    @Override
    public QueueStatus.StatusUpdate status(@Nonnull String endpointId, @Nonnull QueueStatusOptions options) {
        final var endpoint = EndpointId.fromString(endpointId);
        final var url = String.format(
                "https://queue.fal.run/%s/%s/requests/%s/status",
                endpoint.getAppOwner(), endpoint.getAppName(), options.getRequestId());

        final var queryParams = new HashMap<String, Object>();
        if (options.getLogs() != null && options.getLogs()) {
            queryParams.put("logs", "1");
        }

        final var request = httpClient.prepareRequest(url, options, queryParams);
        final var response = httpClient.executeRequest(request);
        final var result = httpClient.handleResponse(response, JsonObject.class);
        return httpClient.fromJson(result, QueueStatus.resolveType(result));
    }

    @Override
    @Nonnull
    public Completed subscribeToStatus(@Nonnull String endpointId, @Nonnull QueueSubscribeOptions options) {
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

        final var future = new CompletableFuture<Completed>();

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
                if (currentStatus != null && currentStatus instanceof Completed) {
                    future.complete((Completed) currentStatus);
                    return;
                }
                future.completeExceptionally(new FalException("Streaming closed with invalid state: " + currentStatus));
            }

            @Override
            public void onFailure(
                    @Nonnull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                future.completeExceptionally(t);
            }
        };
        factory.newEventSource(request, listener);
        try {
            return future.get();
        } catch (Exception ex) {
            throw new FalException(ex.getMessage(), ex);
        }
    }

    @Nonnull
    @Override
    public <O> Result<O> result(@Nonnull String endpointId, @Nonnull QueueResultOptions<O> options) {
        final var endpoint = EndpointId.fromString(endpointId);
        final var url = String.format(
                "https://queue.fal.run/%s/%s/requests/%s",
                endpoint.getAppOwner(), endpoint.getAppName(), options.getRequestId());
        final var request = httpClient.prepareRequest(url, options);

        final var response = httpClient.executeRequest(request);
        return httpClient.wrapInResult(response, options.getResultType());
    }
}
