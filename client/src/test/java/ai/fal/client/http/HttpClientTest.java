package ai.fal.client.http;

import static org.assertj.core.api.Assertions.assertThat;

import ai.fal.client.ClientConfig;
import ai.fal.client.CredentialsResolver;
import ai.fal.client.exception.FalException;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpClientTest {

    private HttpClient httpClient;

    private static final MediaType JSON = MediaType.parse("application/json");

    @BeforeEach
    void setUp() {
        var config = ClientConfig.withCredentials(CredentialsResolver.fromApiKey("test-key"));
        httpClient = new HttpClient(config, new OkHttpClient());
    }

    private Response buildResponse(int code, String contentType, String body) {
        var builder = new Response.Builder()
                .request(new Request.Builder().url("https://queue.fal.run/test").build())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("Error");

        if (body != null && contentType != null) {
            builder.header("content-type", contentType);
            builder.body(ResponseBody.create(body, MediaType.parse(contentType)));
        } else {
            builder.body(ResponseBody.create("", MediaType.parse("text/plain")));
        }

        return builder.build();
    }

    // --- Validation error format: {"detail": [{"msg": "...", ...}]} ---

    @Test
    void responseToException_validationError_extractsMessage() {
        var body =
                "{\"detail\":[{\"loc\":[\"body\",\"prompt\"],\"msg\":\"The prompt may contain intellectual property references that cannot be processed.\",\"type\":\"value_error\"}]}";

        var ex = httpClient.responseToException(buildResponse(422, "application/json", body));

        assertThat(ex.getMessage())
                .isEqualTo(
                        "Request failed with code: 422: The prompt may contain intellectual property references that cannot be processed.");
        assertThat(ex.getStatusCode()).isEqualTo(422);
        assertThat(ex.getBody()).isEqualTo(body);
    }

    @Test
    void responseToException_multipleValidationErrors_joinsThem() {
        var body =
                "{\"detail\":[{\"msg\":\"Field is required\",\"type\":\"missing\"},{\"msg\":\"Invalid format\",\"type\":\"value_error\"}]}";

        var ex = httpClient.responseToException(buildResponse(422, "application/json", body));

        assertThat(ex.getMessage())
                .isEqualTo("Request failed with code: 422: Field is required; Invalid format");
    }

    @Test
    void responseToException_detailArrayWithItemsMissingMsg_skipsItems() {
        var body =
                "{\"detail\":[{\"type\":\"missing\"},{\"msg\":\"Has message\",\"type\":\"value_error\"}]}";

        var ex = httpClient.responseToException(buildResponse(422, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 422: Has message");
    }

    @Test
    void responseToException_detailArrayAllItemsMissingMsg_noDetailInMessage() {
        var body = "{\"detail\":[{\"type\":\"missing\"},{\"type\":\"value_error\"}]}";

        var ex = httpClient.responseToException(buildResponse(422, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 422");
    }

    @Test
    void responseToException_detailAsString_extractsIt() {
        var body = "{\"detail\":\"Not authenticated\"}";

        var ex = httpClient.responseToException(buildResponse(401, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 401: Not authenticated");
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    void responseToException_emptyDetailArray_noDetailInMessage() {
        var body = "{\"detail\":[]}";

        var ex = httpClient.responseToException(buildResponse(422, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 422");
    }

    // --- {"message": "..."} format ---

    @Test
    void responseToException_messageFormat_extractsMessage() {
        var body = "{\"message\":\"Internal server error\"}";

        var ex = httpClient.responseToException(buildResponse(500, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 500: Internal server error");
    }

    // --- {"error": "..."} format ---

    @Test
    void responseToException_errorStringFormat_extractsError() {
        var body = "{\"error\":\"Rate limit exceeded\"}";

        var ex = httpClient.responseToException(buildResponse(429, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 429: Rate limit exceeded");
    }

    @Test
    void responseToException_errorObjectFormat_noDetailInMessage() {
        var body = "{\"error\":{\"code\":\"RATE_LIMIT\",\"info\":\"too many requests\"}}";

        var ex = httpClient.responseToException(buildResponse(429, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 429");
        assertThat(ex.getBody()).isEqualTo(body);
    }

    // --- Priority: detail > message > error ---

    @Test
    void responseToException_detailTakesPriorityOverMessage() {
        var body = "{\"detail\":\"Auth failed\",\"message\":\"Something went wrong\"}";

        var ex = httpClient.responseToException(buildResponse(401, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 401: Auth failed");
    }

    // --- Non-JSON and edge cases ---

    @Test
    void responseToException_nonJsonContentType_noBodyParsed() {
        var ex = httpClient.responseToException(buildResponse(500, "text/html", "<h1>Error</h1>"));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 500");
        assertThat(ex.getBody()).isNull();
        assertThat(ex.getStatusCode()).isEqualTo(500);
    }

    @Test
    void responseToException_invalidJson_noDetailInMessage() {
        var ex = httpClient.responseToException(
                buildResponse(500, "application/json", "not json at all"));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 500");
        assertThat(ex.getBody()).isEqualTo("not json at all");
    }

    @Test
    void responseToException_emptyJsonObject_noDetailInMessage() {
        var ex = httpClient.responseToException(buildResponse(500, "application/json", "{}"));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 500");
    }

    @Test
    void responseToException_jsonArray_noDetailInMessage() {
        var ex = httpClient.responseToException(buildResponse(500, "application/json", "[1,2,3]"));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 500");
    }

    @Test
    void responseToException_emptyBody_noDetailInMessage() {
        var ex = httpClient.responseToException(buildResponse(500, "application/json", ""));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 500");
    }

    @Test
    void responseToException_nullFields_noDetailInMessage() {
        var body = "{\"detail\":null,\"message\":null,\"error\":null}";

        var ex = httpClient.responseToException(buildResponse(500, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 500");
    }

    @Test
    void responseToException_detailNumberNotString_noDetailInMessage() {
        var body = "{\"detail\":42}";

        var ex = httpClient.responseToException(buildResponse(400, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 400");
    }

    @Test
    void responseToException_detailBooleanNotString_noDetailInMessage() {
        var body = "{\"detail\":true}";

        var ex = httpClient.responseToException(buildResponse(400, "application/json", body));

        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 400");
    }

    // --- requestId header ---

    @Test
    void responseToException_preservesRequestId() {
        var response = new Response.Builder()
                .request(new Request.Builder().url("https://queue.fal.run/test").build())
                .protocol(Protocol.HTTP_1_1)
                .code(422)
                .message("Error")
                .header("X-Fal-Request-Id", "req-123")
                .header("content-type", "application/json")
                .body(ResponseBody.create("{\"detail\":\"test\"}", JSON))
                .build();

        var ex = httpClient.responseToException(response);

        assertThat(ex.getRequestId()).isEqualTo("req-123");
        assertThat(ex.getMessage()).isEqualTo("Request failed with code: 422: test");
    }

    @Test
    void responseToException_noRequestIdHeader_returnsNull() {
        var ex = httpClient.responseToException(buildResponse(500, "application/json", "{}"));

        assertThat(ex.getRequestId()).isNull();
    }

    // --- FalException backward compatibility ---

    @Test
    void falException_legacyConstructor_defaultsStatusCodeAndBody() {
        var ex = new FalException("some error", "req-1");

        assertThat(ex.getMessage()).isEqualTo("some error");
        assertThat(ex.getRequestId()).isEqualTo("req-1");
        assertThat(ex.getStatusCode()).isEqualTo(-1);
        assertThat(ex.getBody()).isNull();
    }

    @Test
    void falException_causeConstructor_defaultsStatusCodeAndBody() {
        var cause = new RuntimeException("cause");
        var ex = new FalException("wrapped", cause, "req-2");

        assertThat(ex.getMessage()).isEqualTo("wrapped");
        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getStatusCode()).isEqualTo(-1);
        assertThat(ex.getBody()).isNull();
    }

    @Test
    void falException_throwableOnlyConstructor_defaultsAll() {
        var cause = new IOException("network");
        var ex = new FalException(cause);

        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getRequestId()).isNull();
        assertThat(ex.getStatusCode()).isEqualTo(-1);
        assertThat(ex.getBody()).isNull();
    }
}
