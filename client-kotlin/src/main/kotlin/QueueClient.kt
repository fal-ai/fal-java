package ai.fal.client.kt

import ai.fal.client.Result
import ai.fal.client.queue.AsyncQueueClient
import ai.fal.client.queue.QueueStatus
import kotlinx.coroutines.future.await
import kotlin.reflect.KClass
import ai.fal.client.queue.QueueResultOptions as InternalResultOptions
import ai.fal.client.queue.QueueStatusOptions as InternalStatusOptions
import ai.fal.client.queue.QueueSubmitOptions as InternalSubmitOptions
import ai.fal.client.queue.QueueSubscribeOptions as InternalSubscribeOptions

data class SubmitOptions(
    val webhookUrl: String? = null,
)

data class StatusOptions(
    val logs: Boolean = false,
)

data class StatusSubscribeOptions(
    val logs: Boolean = false,
)

/**
 * A Kotlin queue client for interacting with the fal queue APIs.
 * @see AsyncQueueClient
 */
interface QueueClient {
    /**
     * Submits a request to the given [endpointId]. This method
     * uses the Queue API to submit the request and returns the initial
     * status of the request.
     *
     * @param endpointId The ID of the endpoint to send the request to.
     * @param input The input data to send to the endpoint.
     * @param options The options to use for the request.
     *
     * @see #status
     * @see #result
     */
    suspend fun <Input> submit(
        endpointId: String,
        input: Input,
        options: SubmitOptions = SubmitOptions(),
    ): QueueStatus.InQueue

    /**
     * Gets the current status of the request with the given [requestId].
     *
     * @param endpointId The ID of the endpoint to send the request to.
     * @param requestId The ID of the request to get the status for.
     * @param options The options to use for the request.
     *
     * @see #submit
     */
    suspend fun status(
        endpointId: String,
        requestId: String,
        options: StatusOptions = StatusOptions(),
    ): QueueStatus.StatusUpdate

    /**
     * Subscribes to the status updates of the request with the given [requestId].
     * This method uses the Queue API to subscribe to the status updates of the request.
     *
     * @param endpointId The ID of the endpoint to send the request to.
     * @param requestId The ID of the request to subscribe to.
     * @param options The options to use for the request.
     *
     * @see #submit
     * @see #status
     */
    suspend fun subscribeToStatus(
        endpointId: String,
        requestId: String,
        options: StatusSubscribeOptions = StatusSubscribeOptions(),
        onUpdate: OnStatusUpdate? = null,
    ): QueueStatus.Completed

    /**
     * Gets the result of the request with the given `requestId`.
     *
     * @param endpointId The ID of the endpoint to send the request to.
     * @param requestId The ID of the request to get the result for.
     * @param resultType The expected result type of the request.
     *
     * @see #submit
     */
    suspend fun <Output : Any> result(
        endpointId: String,
        requestId: String,
        resultType: KClass<Output>,
    ): Result<Output>
}

/**
 * An implementation of [QueueClient] that wraps the Java [AsyncQueueClient]
 * and offer a coroutine-based contract.
 */
internal class QueueClientImpl(
    private val queueClient: AsyncQueueClient,
) : QueueClient {
    override suspend fun <Input> submit(
        endpointId: String,
        input: Input,
        options: SubmitOptions,
    ): QueueStatus.InQueue {
        return queueClient.submit(
            endpointId,
            InternalSubmitOptions.builder<Any>()
                .input(input)
                .webhookUrl(options.webhookUrl)
                .build(),
        ).await()
    }

    override suspend fun status(
        endpointId: String,
        requestId: String,
        options: StatusOptions,
    ): QueueStatus.StatusUpdate {
        return queueClient.status(
            endpointId,
            InternalStatusOptions.builder()
                .requestId(requestId)
                .logs(options.logs)
                .build(),
        ).await()
    }

    override suspend fun subscribeToStatus(
        endpointId: String,
        requestId: String,
        options: StatusSubscribeOptions,
        onUpdate: OnStatusUpdate?,
    ): QueueStatus.Completed {
        return queueClient.subscribeToStatus(
            endpointId,
            InternalSubscribeOptions.builder()
                .requestId(requestId)
                .logs(options.logs)
                .onUpdate(onUpdate)
                .build(),
        ).await()
    }

    override suspend fun <Output : Any> result(
        endpointId: String,
        requestId: String,
        resultType: KClass<Output>,
    ): Result<Output> {
        return queueClient.result(
            endpointId,
            InternalResultOptions.builder<Output>()
                .requestId(requestId)
                .resultType(resultType.java)
                .build(),
        ).await()
    }
}

suspend inline fun <reified Output : Any> QueueClient.result(
    endpointId: String,
    requestId: String,
): Result<Output> {
    return result(endpointId, requestId, Output::class)
}
