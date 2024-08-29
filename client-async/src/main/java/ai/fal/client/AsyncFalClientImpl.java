package ai.fal.client;

import ai.fal.client.http.ClientProxyInterceptor;
import ai.fal.client.http.CredentialsInterceptor;
import ai.fal.client.http.HttpClient;
import ai.fal.client.queue.AsyncQueueClient;
import ai.fal.client.queue.AsyncQueueClientImpl;
import ai.fal.client.queue.QueueResponseOptions;
import ai.fal.client.queue.QueueSubmitOptions;
import java.util.concurrent.CompletableFuture;
import okhttp3.OkHttpClient;

public class AsyncFalClientImpl implements AsyncFalClient {

    private final HttpClient httpClient;

    private final AsyncQueueClient queueClient;

    AsyncFalClientImpl(ClientConfig config) {
        final var builder = new OkHttpClient.Builder().addInterceptor(new CredentialsInterceptor(config));
        if (config.getProxyUrl() != null) {
            builder.addInterceptor(new ClientProxyInterceptor(config));
        }
        this.httpClient = new HttpClient(config, builder.build());
        this.queueClient = new AsyncQueueClientImpl(this.httpClient);
    }

    @Override
    public <I, O> CompletableFuture<Result<O>> run(String endpointId, RunOptions<I, O> options) {
        final var url = "https://fal.run/" + endpointId;
        final var request = httpClient.prepareRequest(url, options);
        return httpClient
                .executeRequestAsync(request)
                .thenApply(response -> httpClient.wrapInResult(response, options.getResultType()));
    }

    @Override
    public <O> CompletableFuture<Result<O>> subscribe(String endpointId, SubscribeOptions<O> options) {
        return queueClient
                .submit(
                        endpointId,
                        QueueSubmitOptions.builder()
                                .input(options.getInput())
                                .webhookUrl(options.getWebhookUrl())
                                .build())
                .thenCompose((submitted) -> queueClient.subscribeToStatus(endpointId, options))
                .thenCompose((completed) -> queueClient.result(
                        endpointId,
                        QueueResponseOptions.<O>builder()
                                .requestId(completed.getRequestId())
                                .resultType(options.getResultType())
                                .build()));
    }

    @Override
    public AsyncQueueClient queue() {
        return this.queueClient;
    }
}
