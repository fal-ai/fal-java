package ai.fal.client;

import ai.fal.client.http.ClientProxyInterceptor;
import ai.fal.client.http.CredentialsInterceptor;
import ai.fal.client.http.HttpClient;
import ai.fal.client.queue.*;
import jakarta.annotation.Nonnull;
import okhttp3.OkHttpClient;

public class FalClientImpl implements FalClient {

    private final HttpClient httpClient;
    private final QueueClient queueClient;

    FalClientImpl(@Nonnull ClientConfig config) {
        final var builder = new OkHttpClient.Builder().addInterceptor(new CredentialsInterceptor(config));
        if (config.getProxyUrl() != null) {
            builder.addInterceptor(new ClientProxyInterceptor(config));
        }
        this.httpClient = new HttpClient(config, builder.build());
        this.queueClient = new QueueClientImpl(this.httpClient);
    }

    @Override
    @Nonnull
    public <I, O> Result<O> run(String endpointId, RunOptions<I, O> options) {
        final var url = "https://fal.run/" + endpointId;
        final var request = httpClient.prepareRequest(url, options);
        final var response = httpClient.executeRequest(request);
        return httpClient.wrapInResult(response, options.getResultType());
    }

    @Override
    @Nonnull
    public <O> Result<O> subscribe(String endpointId, SubscribeOptions<O> options) {
        final var enqueued = queueClient.submit(
                endpointId,
                QueueSubmitOptions.builder()
                        .input(options.getInput())
                        .webhookUrl(options.getWebhookUrl())
                        .build());

        final var completed = queueClient.subscribeToStatus(
                endpointId,
                QueueSubscribeOptions.builder()
                        .requestId(enqueued.getRequestId())
                        .logs(options.getLogs())
                        .onUpdate(options.getOnUpdate())
                        .build());

        return queueClient.result(
                endpointId,
                QueueResultOptions.<O>builder()
                        .requestId(completed.getRequestId())
                        .resultType(options.getResultType())
                        .build());
    }

    @Override
    public QueueClient queue() {
        return this.queueClient;
    }
}
