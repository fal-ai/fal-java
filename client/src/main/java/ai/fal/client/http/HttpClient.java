package ai.fal.client.http;

import ai.fal.client.ApiOptions;
import ai.fal.client.ClientConfig;
import ai.fal.client.Output;
import ai.fal.client.exception.FalException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {

    private static final String APPLICATION_JSON = "application/json";

    private static final String HEADER_REQUEST_ID = "X-Fal-Request-Id";

    private final ClientConfig config;
    private final OkHttpClient client;
    private final Gson gson;

    public HttpClient(@Nonnull ClientConfig config, @Nonnull OkHttpClient client) {
        this.config = config;
        this.client = client;
        this.gson = new Gson();
    }

    @Nonnull
    public Request prepareRequest(@Nonnull String url, @Nonnull ApiOptions options) {
        return prepareRequest(url, options, Collections.EMPTY_MAP);
    }

    @Nonnull
    public Request prepareRequest(
            @Nonnull String url, @Nonnull ApiOptions options, @Nonnull Map<String, Object> queryParams) {
        var body = options.getInput() != null ? gson.toJson(options.getInput()) : null;
        var urlBuilder = HttpUrl.parse(url).newBuilder();
        if (!queryParams.isEmpty()) {
            queryParams.forEach((key, value) -> urlBuilder.addQueryParameter(key, value.toString()));
        }
        final var httpMethod = Optional.ofNullable(options.getHttpMethod()).orElse("POST");
        return new Request.Builder()
                .addHeader("content-type", "application/json")
                .addHeader("accept", "application/json")
                .method(
                        httpMethod,
                        !httpMethod.equalsIgnoreCase("GET") && body != null
                                ? RequestBody.create(body, MediaType.parse(APPLICATION_JSON))
                                : null)
                .url(urlBuilder.build().url())
                .build();
    }

    public Response executeRequest(Request request) {
        try {
            return client.newCall(request).execute();
        } catch (IOException ex) {
            throw new FalException(ex);
        }
    }

    public CompletableFuture<Response> executeRequestAsync(Request request) {
        var future = new CompletableFuture<Response>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                future.complete(response);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public <T> T handleResponse(Response response, Class<T> resultType) {
        final var requestId = response.header(HEADER_REQUEST_ID);
        if (!response.isSuccessful()) {
            throw responseToException(response);
        }
        final var body = response.body();
        if (body == null) {
            throw new FalException("Response has empty body", requestId);
        }
        return gson.fromJson(body.charStream(), resultType);
    }

    public FalException responseToException(Response response) {
        final var requestId = response.header(HEADER_REQUEST_ID);
        final var contentType = response.header("content-type");
        if (contentType != null && contentType.contains("application/json")) {
            final var body = response.body();
            if (body != null) {
                final var json = gson.fromJson(body.charStream(), JsonElement.class);
            }
        }

        return new FalException("Request failed with code: " + response.code(), requestId);
    }

    public <T> Output<T> wrapInResult(Response response, Class<T> resultType) {
        final String requestId = response.header(HEADER_REQUEST_ID);
        return new Output<>(handleResponse(response, resultType), requestId);
    }

    public <T> T fromJson(JsonElement json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    public <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    public OkHttpClient getUnderlyingClient() {
        return client;
    }
}
