package ai.fal.client.kt

import java.util.concurrent.CompletableFuture
import ai.fal.client.Output as InternalOutput

/**
 * A data class that represents the output of a request.
 *
 * @param requestId The ID of the request.
 * @param data The output data of the request.
 */
data class RequestOutput<Output>(
    val data: Output,
    val requestId: String,
)

/**
 * Converts an Java's [InternalOutput] to a [RequestOutput].
 * The data class allows specific Kotlin functionality, such as destructuring.
 */
internal fun <Output> CompletableFuture<InternalOutput<Output>>.thenConvertOutput(): CompletableFuture<RequestOutput<Output>> {
    return thenApply { RequestOutput(it.data, it.requestId) }
}
