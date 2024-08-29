package ai.fal.client.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class EndpointId {

    private static final List<String> RESERVED_NAMESPACES = Arrays.asList("workflows", "comfy");

    private final String appOwner;

    private final String appName;

    private final String path;

    private final String namespace;

    public EndpointId(
            @Nonnull String appOwner, @Nonnull String appName, @Nullable String path, @Nullable String namespace) {
        this.appOwner = appOwner;
        this.appName = appName;
        this.path = path;
        this.namespace = namespace;
    }

    @Nonnull
    public String getAppOwner() {
        return appOwner;
    }

    @Nonnull
    public String getAppName() {
        return appName;
    }

    @Nullable
    public String getPath() {
        return path;
    }

    @Nullable
    public String getNamespace() {
        return namespace;
    }

    public static EndpointId fromString(String endpointId) {
        final String[] parts = endpointId.split("/");

        if (RESERVED_NAMESPACES.contains(parts[0])) {
            return new EndpointId(
                    parts[1],
                    parts[2],
                    parts.length > 3 ? String.join("/", Arrays.copyOfRange(parts, 3, parts.length)) : null,
                    parts[0]);
        }

        return new EndpointId(
                parts[0],
                parts[1],
                parts.length > 2 ? String.join("/", Arrays.copyOfRange(parts, 2, parts.length)) : null,
                null);
    }
}
