package ai.fal.client;

import ai.fal.client.queue.AsyncQueueClient;
import jakarta.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public interface AsyncFalClient {

    <O> CompletableFuture<Output<O>> run(String endpointId, RunOptions<O> options);

    <O> CompletableFuture<Output<O>> subscribe(String endpointId, SubscribeOptions<O> options);

    AsyncQueueClient queue();

    /**
     * Create a new client instance with the provided configuration.
     *
     * @param config The client configuration.
     * @return The new client instance.
     */
    static AsyncFalClient withConfig(@Nonnull ClientConfig config) {
        return new AsyncFalClientImpl(config);
    }

    /**
     * Create a new client instance with the credentials resolved from the `FAL_KEY` environment
     * variable.
     *
     * @return The new client instance.
     */
    static AsyncFalClient withEnvCredentials() {
        return new AsyncFalClientImpl(ClientConfig.withCredentials(CredentialsResolver.fromEnv()));
    }

    /**
     * Create a new client instance with the provided proxy URL. With this configuration all
     * requests will be proxied through the provided URL and the fal target url will be in a request
     * header called `X-Fal-Target-Url`.
     *
     * @param proxyUrl The proxy URL.
     * @return The new client instance.
     */
    static AsyncFalClient withProxyUrl(@Nonnull String proxyUrl) {
        return new AsyncFalClientImpl(
                ClientConfig.builder().withProxyUrl(proxyUrl).build());
    }
}
