package ai.fal.client;

import ai.fal.client.http.ClientProxyInterceptor;
import ai.fal.client.http.CredentialsInterceptor;
import ai.fal.client.http.FalException;
import ai.fal.client.http.HttpClient;
import ai.fal.client.queue.QueueClient;
import ai.fal.client.queue.QueueClientImpl;
import ai.fal.client.queue.QueueResponseOptions;
import ai.fal.client.queue.QueueStatus;
import ai.fal.client.queue.QueueStatusOptions;
import ai.fal.client.queue.QueueSubmitOptions;
import jakarta.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

        CompletableFuture<Result<O>> resultFuture = new CompletableFuture<>();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(
                () -> {
                    try {
                        final var status = queueClient.status(
                                endpointId,
                                QueueStatusOptions.builder()
                                        .requestId(enqueued.getRequestId())
                                        .logs(options.getLogs())
                                        .build());

                        final var onUpdate = options.getOnUpdate();
                        if (onUpdate != null) {
                            onUpdate.accept(status);
                        }
                        if (status instanceof QueueStatus.Completed) {
                            Result<O> result = queueClient.result(
                                    endpointId,
                                    QueueResponseOptions.<O>builder()
                                            .requestId(enqueued.getRequestId())
                                            .resultType(options.getResultType())
                                            .build());
                            resultFuture.complete(result);
                            executor.shutdown();
                        }
                    } catch (Exception e) {
                        resultFuture.completeExceptionally(e);
                        executor.shutdown();
                    }
                },
                0,
                250,
                TimeUnit.MILLISECONDS);

        try {
            return resultFuture.get(1, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            executor.shutdown();
            throw new FalException("Failed to get result", e);
        }
    }

    @Override
    public QueueClient queue() {
        return this.queueClient;
    }
}
