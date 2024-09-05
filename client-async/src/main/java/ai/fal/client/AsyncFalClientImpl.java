package ai.fal.client;

import ai.fal.client.http.ClientProxyInterceptor;
import ai.fal.client.http.CredentialsInterceptor;
import ai.fal.client.http.HttpClient;
import ai.fal.client.queue.*;
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
    public <O> CompletableFuture<Output<O>> run(String endpointId, RunOptions<O> options) {
        final var url = "https://fal.run/" + endpointId;
        final var request = httpClient.prepareRequest(url, options);
        return httpClient
                .executeRequestAsync(request)
                .thenApply(response -> httpClient.wrapInResult(response, options.getResultType()));
    }

    @Override
    public <O> CompletableFuture<Output<O>> subscribe(String endpointId, SubscribeOptions<O> options) {
        return queueClient
                .submit(
                        endpointId,
                        QueueSubmitOptions.builder()
                                .input(options.getInput())
                                .webhookUrl(options.getWebhookUrl())
                                .build())
                .thenCompose((submitted) -> queueClient.subscribeToStatus(
                        endpointId,
                        QueueSubscribeOptions.builder()
                                .requestId(submitted.getRequestId())
                                .logs(options.getLogs())
                                .onQueueUpdate(options.getOnQueueUpdate())
                                .build()))
                .thenCompose((completed) -> queueClient.result(
                        endpointId,
                        QueueResultOptions.<O>builder()
                                .requestId(completed.getRequestId())
                                .resultType(options.getResultType())
                                .build()));
    }

    @Override
    public AsyncQueueClient queue() {
        return this.queueClient;
    }
}
