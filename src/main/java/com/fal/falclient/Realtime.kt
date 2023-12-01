import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

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
    return "$protocol://$app.$host/ws${token?.let { "?fal_jwt_token=$it" } ?: ""}"
}

class WebSocketConnection(
    private val app: String,
    private val onMessage: (String) -> Unit, // Modified for simplicity to String
    private val onError: (Throwable) -> Unit
) : WebSocketListener() {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(token: String?) {
        val url = buildRealtimeUrl(app, "gateway.alpha.fal.ai", token)
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, this)
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
