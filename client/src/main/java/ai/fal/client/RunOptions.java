package ai.fal.client;

import com.google.gson.JsonObject;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RunOptions<O> implements ApiOptions<O> {

    private final Object input;
    private final String httpMethod;
    private final Class<O> resultType;

    public static <O> RunOptions<O> withInput(@Nonnull Object input, @Nonnull Class<O> resultType) {
        return RunOptions.<O>builder().input(input).resultType(resultType).build();
    }

    public static RunOptions<JsonObject> withInput(@Nonnull Object input) {
        return withInput(input, JsonObject.class);
    }
}
