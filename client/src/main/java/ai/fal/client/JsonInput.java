package ai.fal.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonInput {

    private final JsonObject input;

    JsonInput(JsonObject input) {
        this.input = input;
    }

    public static JsonInput input() {
        return new JsonInput(new JsonObject());
    }

    public JsonInput set(String key, String value) {
        input.addProperty(key, value);
        return this;
    }

    public JsonInput set(String key, Number value) {
        input.addProperty(key, value);
        return this;
    }

    public JsonInput set(String key, Boolean value) {
        input.addProperty(key, value);
        return this;
    }

    public JsonInput set(String key, Character value) {
        input.addProperty(key, value);
        return this;
    }

    public JsonInput set(String key, JsonObject value) {
        input.add(key, value);
        return this;
    }

    public JsonInput set(String key, JsonArray value) {
        input.add(key, value);
        return this;
    }

    public JsonObject build() {
        return input;
    }
}
