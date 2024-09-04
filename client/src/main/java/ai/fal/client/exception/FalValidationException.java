package ai.fal.client.exception;

import com.google.gson.annotations.SerializedName;
import jakarta.annotation.Nonnull;
import java.util.List;
import lombok.Data;

public class FalValidationException {

    private final List<ValidationError> errors;

    public FalValidationException(List<ValidationError> errors) {
        this.errors = errors;
    }

    @Data
    public static class ValidationError {
        @SerializedName("msg")
        private final String message;

        @SerializedName("loc")
        private List<Object> location;

        @Nonnull
        private final String type;
    }
}
