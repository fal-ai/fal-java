package ai.fal.client;

import com.google.gson.JsonObject;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RunOptions<I, O> implements ApiOptions<I, O> {

    private final I input;
    private final String httpMethod;
    private final Class<O> resultType;

    public static <Input> RunOptions<Input, JsonObject> withInput(@Nonnull Input input) {
        return RunOptions.<Input, JsonObject>builder()
                .input(input)
                .resultType(JsonObject.class)
                .build();
    }
}
