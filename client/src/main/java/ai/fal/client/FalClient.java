package ai.fal.client;

import ai.fal.client.queue.QueueClient;
import jakarta.annotation.Nonnull;

/**
 * The main client interface for interacting with the FAL APIs.
 *
 * @see {@link FalClient#withConfig(ClientConfig)} method to create a new client instance with the
 *     provided configuration.
 * @see {@link FalClient#withEnvCredentials()} method to create a new client instance with the
 *     credentials resolved from the environment.
 */
public interface FalClient {

    /**
     * Run the specified endpoint with the provided options. This method is only recommended for
     * short-running operations. For long-running operations, consider using the {@link
     * #subscribe(String, SubscribeOptions<O>)} method to subscribe to the endpoint's results via
     * the queue or {@link #queue()} client for specific queue operations. .
     *
     * @param <I> Input type.
     * @param <O> Output type.
     * @param endpointId The endpoint ID to run, e.g. `fal-ai/fast-sdxl`.
     * @param options The run options.
     * @return The result of the operation.
     */
    <I, O> Result<O> run(String endpointId, RunOptions<I, O> options);

    /**
     * Subscribe to the specified endpoint with the provided options. This method is recommended for
     * long-running operations. The subscription will return the result once the operation is
     * completed.
     *
     * @param <O> Output type.
     * @param endpointId The endpoint ID to subscribe to, e.g. `fal-ai/fast-sdxl`.
     * @param options The subscribe options.
     * @return The result of the operation.
     * @see #queue()
     */
    <O> Result<O> subscribe(String endpointId, SubscribeOptions<O> options);

    /**
     * Get the queue client for interacting with the FAL queue.
     *
     * @return The queue client.
     */
    QueueClient queue();

    /**
     * Create a new client instance with the provided configuration.
     *
     * @param config The client configuration.
     * @return The new client instance.
     */
    public static FalClient withConfig(@Nonnull ClientConfig config) {
        return new FalClientImpl(config);
    }

    /**
     * Create a new client instance with the credentials resolved from the `FAL_KEY` environment
     * variable.
     *
     * @return The new client instance.
     */
    public static FalClient withEnvCredentials() {
        return new FalClientImpl(ClientConfig.withCredentials(CredentialsResolver.fromEnv()));
    }

    /**
     * Create a new client instance with the provided proxy URL. With this configuration all
     * requests will be proxied through the provided URL and the fal target url will be in a request
     * header called `X-Fal-Target-Url`.
     *
     * @param proxyUrl The proxy URL.
     * @return The new client instance.
     */
    public static FalClient withProxyUrl(@Nonnull String proxyUrl) {
        return new FalClientImpl(ClientConfig.builder().withProxyUrl(proxyUrl).build());
    }
}
