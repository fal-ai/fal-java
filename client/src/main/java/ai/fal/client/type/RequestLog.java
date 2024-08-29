package ai.fal.client.type;

import jakarta.annotation.Nonnull;
import lombok.Data;

@Data
public class RequestLog {
    public enum Level {
        STDERR,
        STDOUT,
        ERROR,
        INFO,
        WARN,
        DEBUG
    }

    @Nonnull
    private final String message;

    @Nonnull
    private final String timestamp;

    @Nonnull
    private final LogLabels labels;

    public Level getLevel() {
        return labels.getLevel();
    }

    @Data
    public static class LogLabels {

        @Nonnull
        private final Level level;

        @Nonnull
        private final String source;
    }
}
