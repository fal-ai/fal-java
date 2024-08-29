package ai.fal.client.kt

import ai.fal.client.AsyncFalClient
import ai.fal.client.ClientConfig
import ai.fal.client.CredentialsResolver
import ai.fal.client.RunOptions

public interface FalClient {
    suspend fun <Input, Output> run(
        endpointId: String,
        options: RunOptions<Input, Output>,
    ): Result<Output>

//    suspend fun <Input, Output>subscribe(endpointId: String, options: SubscribeOptions<>): Result<Output>
}

class FalClientKotlinImpl(
    config: ClientConfig,
) : FalClient {
    val client = AsyncFalClient.withConfig(config)

    override suspend fun <Input, Output> run(
        endpointId: String,
        options: RunOptions<Input, Output>,
    ): Result<Output> {
        return client.run(endpointId, options).await()
    }

//    suspend fun <Input, Output>subscribe(endpointId: String, options: SubscribeOptions<>): Result<Output> {
//        TODO()
//    }
}

public fun createFalClient(config: ClientConfig? = null): FalClient =
    FalClientKotlinImpl(config ?: ClientConfig.withCredentials(CredentialsResolver.fromEnv()))
