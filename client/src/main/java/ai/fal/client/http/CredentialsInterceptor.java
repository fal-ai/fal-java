package ai.fal.client.http;

import ai.fal.client.ClientConfig;
import jakarta.annotation.Nonnull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class CredentialsInterceptor implements Interceptor {

    private final ClientConfig config;

    public CredentialsInterceptor(@Nonnull ClientConfig config) {
        this.config = config;
    }

    @Override
    @Nonnull
    public Response intercept(@NotNull Chain chain) throws IOException {
        var resolver = config.getCredentials();
        var credentials = resolver.get();
        if (credentials != null) {
            var request = chain.request()
                    .newBuilder()
                    .header("Authorization", "Key " + credentials)
                    .build();
            return chain.proceed(request);
        }
        return chain.proceed(chain.request());
    }
}
