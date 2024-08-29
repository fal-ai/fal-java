package ai.fal.client.queue;

import ai.fal.client.Result;
import ai.fal.client.http.HttpClient;
import ai.fal.client.util.EndpointId;
import com.google.gson.JsonObject;
import jakarta.annotation.Nonnull;
import java.util.HashMap;

public class QueueClientImpl implements QueueClient {

    private final HttpClient httpClient;

    public QueueClientImpl(@Nonnull HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Nonnull
    @Override
    public <I> QueueStatus.InQueue submit(String endpointId, QueueSubmitOptions<I> options) {
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

    @Nonnull
    @Override
    public <O> Result<O> result(@Nonnull String endpointId, @Nonnull QueueResponseOptions<O> options) {
        final var endpoint = EndpointId.fromString(endpointId);
        final var url = String.format(
                "https://queue.fal.run/%s/%s/requests/%s",
                endpoint.getAppOwner(), endpoint.getAppName(), options.getRequestId());
        final var request = httpClient.prepareRequest(url, options);

        final var response = httpClient.executeRequest(request);
        return httpClient.wrapInResult(response, options.getResultType());
    }
}
