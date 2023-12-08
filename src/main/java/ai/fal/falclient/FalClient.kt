package ai.fal.falclient
import kotlinx.coroutines.delay
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class ClientConfig(
    val credentials: String,
    val requestProxy: String? = null
)

enum class ClientCredentials {
    KEY_PAIR, KEY, FROM_ENV, CUSTOM;

    fun getCredentials(keyPair: String? = null, id: String? = null, secret: String? = null, resolver: (() -> String)? = null): String {
        return when (this) {
            KEY_PAIR -> keyPair ?: ""
            KEY -> "$id:$secret"
            FROM_ENV -> System.getenv("FAL_KEY") ?: "${System.getenv("FAL_KEY_ID")}:${System.getenv("FAL_KEY_SECRET")}"
            CUSTOM -> resolver?.invoke() ?: ""
        }
    }
}

data class RunOptions(
    val httpMethod: HttpMethod,
    val path: String = "/"
) {
    fun toQueryMap(): Map<String, String> {
        return mutableMapOf(
            "httpMethod" to this.httpMethod.toString(),
            "path" to this.path
        )
    }
}

enum class HttpMethod { GET, POST }

class FalClient(private val config: ClientConfig) {
    private val falService: FalService

    init {
        val httpClient = OkHttpClient.Builder().addInterceptor(createInterceptor())
        val retrofit = Retrofit.Builder()
            .baseUrl("https://gateway.alpha.fal.ai")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()

        falService = retrofit.create(FalService::class.java)
    }

    private fun createInterceptor() = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder().header("Authorization", "Key " + config.credentials)
        chain.proceed(requestBuilder.build())
    }

    suspend fun run(app: String, input: HashMap<String, String>, options: RunOptions = RunOptions(HttpMethod.POST)): HashMap<String, Any> {
        val url = buildUrl(app, options.path)
        val response = falService.run(url, input, options.toQueryMap())
        checkResponseValidity(response)
        return response.body() ?: throw FalError.InvalidResultFormat()
    }

    suspend fun subscribe(
        app: String,
        input: Map<String, Any>?,
        pollInterval: Long,
        timeout: Long,
        includeLogs: Boolean,
        onQueueUpdate: ((HashMap<String, Any>) -> Unit)?
    ) {
        val requestIdResponse = falService.submitToQueue(buildUrl(app, "/fal/queue/submit"), input as HashMap<String, Any>?)
        val requestId = requestIdResponse.body()?.get("request_id") ?: throw FalError("Invalid request id")
        val start = System.currentTimeMillis()
        var isCompleted = false

        while (System.currentTimeMillis() - start < timeout) {
            val update = falService.getQueueStatus(buildUrl(app, "/fal/queue/requests/$requestId/status"), includeLogs).body()
                ?: throw FalError("Invalid update status")
            onQueueUpdate?.invoke(update)
            isCompleted = update["status"] == "COMPLETED"
            if (isCompleted) break
            delay(pollInterval)
        }

        if (!isCompleted) throw FalError.QueueTimeout()
        val response = falService.getQueueResponse(buildUrl(app, "/fal/queue/requests/$requestId/response")).body() ?: throw FalError("Invalid queue response")
        onQueueUpdate?.invoke(response)
    }


    private fun checkResponseValidity(response: Response<HashMap<String, Any>>) {
        if (!response.isSuccessful) throw FalError("Request failed with status code ${response.code()}")
    }

    private fun buildUrl(app: String, path: String? = null): String {
        return "https://${app}.gateway.alpha.fal.ai${path ?: ""}"
    }
}
