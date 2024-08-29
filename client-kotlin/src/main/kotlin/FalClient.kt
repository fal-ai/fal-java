package ai.fal.client.kt

import ai.fal.client.AsyncFalClient
import ai.fal.client.ClientConfig
import ai.fal.client.CredentialsResolver
import ai.fal.client.Result
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

interface FalClient {
    val queue: QueueClient

    suspend fun <Input, Output : Any> run(
        endpointId: String,
        input: Input,
        resultType: KClass<Output>,
        options: RunOptions = RunOptions(),
    ): Result<Output>

    suspend fun <Input, Output : Any> subscribe(
        endpointId: String,
        input: Input,
        resultType: KClass<Output>,
        options: SubscribeOptions = SubscribeOptions(),
        onUpdate: OnStatusUpdate? = null,
    ): Result<Output>
}

typealias OnStatusUpdate = (update: QueueStatus.StatusUpdate) -> Unit

class FalClientKotlinImpl(
    config: ClientConfig,
) : FalClient {
    private val client = AsyncFalClient.withConfig(config)

    override val queue: QueueClient = QueueClientImpl(client.queue())

    override suspend fun <Input, Output : Any> run(
        endpointId: String,
        input: Input,
        resultType: KClass<Output>,
        options: RunOptions,
    ): Result<Output> {
        val internalOptions =
            InternalRunOptions.builder<Input, Output>()
                .httpMethod(options.httpMethod)
                .input(input)
                .resultType(resultType.java)
                .build()
        return client.run(endpointId, internalOptions).await()
    }

    override suspend fun <Input, Output : Any> subscribe(
        endpointId: String,
        input: Input,
        resultType: KClass<Output>,
        options: SubscribeOptions,
        onUpdate: OnStatusUpdate?,
    ): Result<Output> {
        println(resultType)
        println(options)
        val internalOptions =
            InternalSubscribeOptions.builder<Output>()
                .input(input)
                .resultType(resultType.java)
                .logs(options.logs)
                .onUpdate(onUpdate)
                .build()
        return client.subscribe(endpointId, internalOptions).await()
    }
}

suspend inline fun <Input, reified Output : Any> FalClient.run(
    endpointId: String,
    input: Input,
    options: RunOptions = RunOptions(),
) = this.run(endpointId, input, Output::class, options)

suspend inline fun <Input, reified Output : Any> FalClient.subscribe(
    endpointId: String,
    input: Input,
    options: SubscribeOptions = SubscribeOptions(),
    noinline onUpdate: OnStatusUpdate? = null,
) = this.subscribe(endpointId, input, Output::class, options, onUpdate)

fun createFalClient(config: ClientConfig? = null): FalClient =
    FalClientKotlinImpl(config ?: ClientConfig.withCredentials(CredentialsResolver.fromEnv()))

suspend fun main() {
    val fal = createFalClient(ClientConfig.withCredentials { "544baac1-da1b-49b8-b3d9-4d45c237acdb:7d5c9487c05c555f88016390e3e68904" })
    val input =
        mapOf(
            "prompt" to "a cute shih-tzu puppy",
        )
    val result: Result<JsonObject> = fal.run("fal-ai/fast-sdxl", input)
    println(result.requestId)
    println(result.data)

    println("# --- fal.subscribe")
    val queueResult: Result<JsonObject> =
        fal.subscribe("fal-ai/fast-sdxl", input) {
            println("--------------")
            println(it)
        }
    println(queueResult.requestId)
    println(queueResult.data)

    val submitted =
        fal.queue.submit(
            "fal-ai/fast-sdxl",
            input = input,
            options =
                SubmitOptions(
                    webhookUrl = "https://webhook.site/43397eb6-3c2d-4d72-94c8-4d49d4737751",
                ),
        )
    println("submitted.queuePosition = ${submitted.queuePosition}")
    println("submitted.status = ${submitted.status}")
}
