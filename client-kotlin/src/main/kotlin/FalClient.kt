package ai.fal.client.kt

import ai.fal.client.AsyncFalClient
import ai.fal.client.ClientConfig
import ai.fal.client.CredentialsResolver
import ai.fal.client.queue.QueueStatus
import com.google.gson.JsonObject
import kotlinx.coroutines.future.await
import kotlin.reflect.KClass
import ai.fal.client.RunOptions as InternalRunOptions
import ai.fal.client.SubscribeOptions as InternalSubscribeOptions

data class RunOptions(
    val httpMethod: String = "POST",
)

data class SubscribeOptions(
    val logs: Boolean = false,
    val webhookUrl: String? = null,
)

/**
 * The main client class that provides access to simple API model usage,
 * as well as access to the [queue] APIs.
 * @see AsyncFalClient
 */
interface FalClient {
    /** The queue client with specific methods to interact with the queue API.
     *
     * **Note:** that the [subscribe] method is a convenience method that uses the
     * [queue] client to submit a request and poll for the result.
     */
    val queue: QueueClient

    /**
     * Sends a request to the given [endpointId]. This method is a direct request
     * to the model API and it waits for the processing to complete before returning the result.
     *
     * This is useful for short running requests, but it's not recommended for
     * long running requests, for those see [subscribe].
     *
     * @param endpointId The ID of the endpoint to send the request to.
     * @param input The input data to send to the endpoint.
     * @param resultType The expected result type of the request.
     * @param options The options to use for the request.
     */
    suspend fun <Output : Any> run(
        endpointId: String,
        input: Any,
        resultType: KClass<Output>,
        options: RunOptions = RunOptions(),
    ): RequestOutput<Output>

    /**
     * Submits a request to the given [endpointId]. This method
     * uses the [queue] API to submit the request and poll for the result.
     *
     * This is useful for long running requests, and it's the preffered way
     * to interact with the model APIs.
     *
     * @param endpointId The ID of the endpoint to send the request to.
     * @param input The input data to send to the endpoint.
     * @param resultType The expected result type of the request.
     * @param options The options to use for the request.
     * @param onUpdate A callback to receive status updates from the queue subscription.
     */
    suspend fun <Output : Any> subscribe(
        endpointId: String,
        input: Any,
        resultType: KClass<Output>,
        options: SubscribeOptions = SubscribeOptions(),
        onUpdate: OnStatusUpdate? = null,
    ): RequestOutput<Output>
}

/**
 * A callback for receiving status updates from a queue subscription.
 */
typealias OnStatusUpdate = (update: QueueStatus.StatusUpdate) -> Unit

/**
 * A Kotlin implementation of [FalClient] that wraps the Java [AsyncFalClient].
 */
internal class FalClientKotlinImpl(
    config: ClientConfig,
) : FalClient {
    private val client = AsyncFalClient.withConfig(config)

    override val queue: QueueClient = QueueClientImpl(client.queue())

    override suspend fun <Output : Any> run(
        endpointId: String,
        input: Any,
        resultType: KClass<Output>,
        options: RunOptions,
    ): RequestOutput<Output> {
        val internalOptions =
            InternalRunOptions.builder<Output>()
                .httpMethod(options.httpMethod)
                .input(input)
                .resultType(resultType.java)
                .build()
        return client.run(endpointId, internalOptions).thenConvertOutput().await()
    }

    override suspend fun <Output : Any> subscribe(
        endpointId: String,
        input: Any,
        resultType: KClass<Output>,
        options: SubscribeOptions,
        onUpdate: OnStatusUpdate?,
    ): RequestOutput<Output> {
        println(resultType)
        println(options)
        val internalOptions =
            InternalSubscribeOptions.builder<Output>()
                .input(input)
                .resultType(resultType.java)
                .logs(options.logs)
                .onUpdate(onUpdate)
                .build()
        return client.subscribe(endpointId, internalOptions).thenConvertOutput().await()
    }
}

suspend inline fun <reified Output : Any> FalClient.run(
    endpointId: String,
    input: Any,
    options: RunOptions = RunOptions(),
) = this.run(endpointId, input, Output::class, options)

@JvmName("run_")
suspend fun FalClient.run(
    endpointId: String,
    input: Any,
    options: RunOptions = RunOptions(),
) = this.run(endpointId, input, JsonObject::class, options)

suspend inline fun <reified Output : Any> FalClient.subscribe(
    endpointId: String,
    input: Any,
    options: SubscribeOptions = SubscribeOptions(),
    noinline onUpdate: OnStatusUpdate? = null,
) = this.subscribe(endpointId, input, Output::class, options, onUpdate)

@JvmName("subscribe_")
suspend inline fun FalClient.subscribe(
    endpointId: String,
    input: Any,
    options: SubscribeOptions = SubscribeOptions(),
    noinline onUpdate: OnStatusUpdate? = null,
) = this.subscribe(endpointId, input, JsonObject::class, options, onUpdate)

fun createFalClient(config: ClientConfig? = null): FalClient =
    FalClientKotlinImpl(config ?: ClientConfig.withCredentials(CredentialsResolver.fromEnv()))

fun createFalClient(credentialsResolver: CredentialsResolver): FalClient =
    FalClientKotlinImpl(ClientConfig.withCredentials(credentialsResolver))
