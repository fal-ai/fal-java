package ai.fal.client.http;

import ai.fal.client.ClientConfig;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class ClientProxyInterceptor implements Interceptor {

    public static final String HEADER_TARGET_URL = "X-Fal-Target-Url";

    private final ClientConfig config;

    public ClientProxyInterceptor(@Nonnull ClientConfig config) {
        this.config = config;
    }

    @Override
    @Nonnull
    public Response intercept(@NotNull Chain chain) throws IOException {
        final String proxyUrl = config.getProxyUrl();
        if (proxyUrl == null) {
            return chain.proceed(chain.request());
        }
        Request originalRequest = chain.request();
        HttpUrl originalUrl = originalRequest.url();

        Request.Builder requestBuilder = originalRequest.newBuilder().header(HEADER_TARGET_URL, originalUrl.toString());

        HttpUrl newUrl = HttpUrl.parse(proxyUrl);
        if (newUrl != null) {
            requestBuilder.url(newUrl);
        }

        Request newRequest = requestBuilder.build();
        return chain.proceed(newRequest);
    }
}
