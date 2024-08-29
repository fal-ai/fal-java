package ai.fal.client.http;

import ai.fal.client.ApiOptions;
import ai.fal.client.ClientConfig;
import ai.fal.client.Result;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {

    private static final String APPLICATION_JSON = "application/json";

    private final ClientConfig config;
    private final OkHttpClient client;
    private final Gson gson;

    public HttpClient(@Nonnull ClientConfig config, @Nonnull OkHttpClient client) {
        this.config = config;
        this.client = client;
        this.gson = new Gson();
    }

    public Request prepareRequest(@Nonnull String url, @Nonnull ApiOptions options) {
        return prepareRequest(url, options, Collections.EMPTY_MAP);
    }

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

    public <T> T handleResponse(Response response, Class<T> resultType) {
        if (!response.isSuccessful()) {
            throw new FalException("Request failed with code: " + response.code());
        }
        return gson.fromJson(response.body().charStream(), resultType);
    }

    public <T> Result<T> wrapInResult(Response response, Class<T> resultType) {
        final String requestId = response.header("X-Fal-Request-Id");
        return new Result<>(handleResponse(response, resultType), requestId);
    }

    public <T> T fromJson(JsonElement json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}
