package ai.fal.client.kt

import ai.fal.client.Result
import ai.fal.client.queue.AsyncQueueClient
import ai.fal.client.queue.QueueStatus
import kotlinx.coroutines.future.await
import kotlin.reflect.KClass
import ai.fal.client.queue.QueueResultOptions as InternalResultOptions
import ai.fal.client.queue.QueueStatusOptions as InternalStatusOptions
import ai.fal.client.queue.QueueSubmitOptions as InternalSubmitOptions

data class SubmitOptions(
    val webhookUrl: String? = null,
)

data class StatusOptions(
    val logs: Boolean = false,
)

/**
 * A Kotlin queue client for interacting with the fal queue APIs.
 * @see AsyncQueueClient
 */
interface QueueClient {
    suspend fun <Input> submit(
        endpointId: String,
        input: Input,
        options: SubmitOptions = SubmitOptions(),
    ): QueueStatus.InQueue

    suspend fun status(
        endpointId: String,
        requestId: String,
        options: StatusOptions = StatusOptions(),
    ): QueueStatus.StatusUpdate

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
