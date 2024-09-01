package ai.fal.client;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ClientConfig {

    private CredentialsResolver credentials;

    private String proxyUrl;

    @Nonnull
    public CredentialsResolver getCredentials() {
        return credentials;
    }

    @Nullable
    public String getProxyUrl() {
        return proxyUrl;
    }

    public static Builder builder() {
        return new Builder().withCredentials(CredentialsResolver.fromEnv());
    }

    public static ClientConfig withCredentials(CredentialsResolver credentials) {
        return builder().withCredentials(credentials).build();
    }

    public static ClientConfig withProxyUrl(String proxyUrl) {
        return builder().withProxyUrl(proxyUrl).build();
    }

    public static class Builder {

        private final ClientConfig config = new ClientConfig();

        public Builder withCredentials(CredentialsResolver credentials) {
            config.credentials = credentials;
            return this;
        }

        public Builder withProxyUrl(String proxyUrl) {
            config.proxyUrl = proxyUrl;
            return this;
        }

        public ClientConfig build() {
            return config;
        }
    }
}
