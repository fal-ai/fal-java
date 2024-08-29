package ai.fal.client;

import com.google.gson.JsonObject;

public class main {

    public static void main(String[] args) {
        var fal = FalClient.withConfig(ClientConfig.withCredentials(
                () -> "544baac1-da1b-49b8-b3d9-4d45c237acdb:7d5c9487c05c555f88016390e3e68904"));

        var input = JsonInput.input().set("prompt", "A cute shih-tzu puppy").build();

        //        var enqueued = fal.queue().submit("fal-ai/fast-sdxl", QueueSubmitOptions.withInput(input));
        //        System.out.println(enqueued);
        //
        //        var completedStatus = fal.queue()
        //                .subscribeToStatus(
        //                        "fal-ai/fast-sdxl",
        //                        QueueStatusSubscribeOptions.builder()
        //                                .requestId(enqueued.getRequestId())
        //                                .onUpdate((update) -> System.out.println(update))
        //                                .build());
        //        System.out.println(completedStatus);
        //
        //        var result = fal.queue().result("fal-ai/fast-sdxl",
        // QueueResultOptions.withRequestId(enqueued.getRequestId()));
        var result = fal.subscribe(
                "fal-ai/fast-sdxl",
                SubscribeOptions.<JsonObject>builder()
                        .input(input)
                        .resultType(JsonObject.class)
                        .logs(true)
                        .onUpdate((update) -> System.out.println(update))
                        .build());
        System.out.println(result);
    }
}
