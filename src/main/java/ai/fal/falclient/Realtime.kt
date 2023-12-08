package ai.fal.falclient
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener

const val TOKEN_EXPIRATION_SECONDS = 120
val client = OkHttpClient()
val gson = Gson()

fun getToken(app: String, authKey: String): String {
    val appAlias = app.split('-').drop(1).joinToString("-")
    val jsonType = "application/json; charset=utf-8".toMediaType()
    val bodyData = mapOf(
        "allowed_apps" to listOf(appAlias),
        "token_expiration" to TOKEN_EXPIRATION_SECONDS
    )
    val requestBody: RequestBody = gson.toJson(bodyData).toRequestBody(jsonType)

    val request = Request.Builder()
        .url("https://rest.alpha.fal.ai/tokens/")
        .post(requestBody)
        .addHeader("Authorization", "Key $authKey")
        .build()

    client.newCall(request).execute().use { response ->
        val responseBody = response.body?.string() ?: ""
        // Assuming the token is returned directly in the response. Adjust based on the actual API response.
        return if (response.isSuccessful && responseBody.isNotBlank()) {
            responseBody
        } else {
            throw Exception("Failed to get token: ${response.message}")
        }
    }
}


abstract class RealtimeConnection<Input>(private val sendFunction: (Input) -> Unit, private val closeFunction: () -> Unit) {
    fun send(input: Input) = sendFunction(input)
    fun close() = closeFunction()
}

class UntypedRealtimeConnection(send: (String) -> Unit, close: () -> Unit) : RealtimeConnection<String>(send, close) {
    private val gson = Gson()

    fun send(data: Map<String, Any>) {
        val jsonString = gson.toJson(data)
        super.send(jsonString)
    }
}

fun buildRealtimeUrl(app: String, host: String, token: String?): String {
    val protocol = if (token != null) "wss" else "ws"
    val cleanToken = token?.trim('"')
    return "$protocol://$app.$host/ws${cleanToken?.let { "?fal_jwt_token=$it" } ?: ""}"
}

class WebSocketConnection(
    private val app: String,
    private val onMessage: (String) -> Unit, // Modified for simplicity to String
    private val onError: (Throwable) -> Unit
) : WebSocketListener() {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var token: String? = null
    private var job: Job? = null

    suspend fun connect(authKey: String) {
        CoroutineScope(Dispatchers.IO).launch {
            token = getToken(app, authKey)
            val url = buildRealtimeUrl(app, "gateway.alpha.fal.ai", token)
            val request = Request.Builder().url(url).build()
            webSocket = client.newWebSocket(request, this@WebSocketConnection)

            // Refresh token periodically
            job = launch {
                while (isActive) {
                    delay(TOKEN_EXPIRATION_SECONDS * 900L)
                    token = getToken(app, authKey)
                }
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        onMessage.invoke(text)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        onError.invoke(t)
    }

    fun send(message: String) {
        webSocket?.send(message) ?: run { onError.invoke(IllegalStateException("WebSocket not connected")) }
    }

    fun close() {
        webSocket?.close(1000, "Programmatically closed")
        webSocket = null
    }

    fun isConnected(): Boolean {
        return webSocket != null
    }
}

fun throttle(coroutineScope: CoroutineScope, intervalMs: Long, action: (String) -> Unit) : (String) -> Unit {
    val channel = Channel<String>(capacity = Channel.CONFLATED)
    coroutineScope.launch {
        for (msg in channel) {
            action(msg)
            delay(intervalMs)
        }
    }
    return { message ->
        coroutineScope.launch {
            channel.send(message)
        }
    }
}
