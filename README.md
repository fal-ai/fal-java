## The fal.ai Kotlin Client

![License](https://img.shields.io/badge/license-MIT-blue)

## About the Project

The `FalClient` is a robust and user-friendly Kotlin package designed for seamless integration of fal serverless functions into Kotlin projects, including Android Platform.

## Getting Started

The `FalClient` library serves as a client for fal serverless Python functions. Before using this library, ensure you've got your fal key from [our dashboard](https://fal.ai/dashboard/keys).

### Install

Add `falclient` dependency to your build file. Here is the `build.gradle.kts` example:

```
implementation("ai.fal:falclient:0.1.1")
```

### Client Library
```
import ai.fal.falclient.*

// Initialize FalClient with your credentials
val authKey = "$FAL_KEY"
val falClient = FalClient(ClientConfig(credentials = authKey))

// Define the input parameters for the function
val inputParams = hashMapOf(
    "prompt" to "a cute shih-tzu puppy",
    "model_name" to "stabilityai/stable-diffusion-xl-base-1.0",
    "image_size" to "square_hd"
)

// Run the function and print the result
try {
    val result = falClient.run("110602490-lora", inputParams)
    println(result)
} catch (e: Error) {
    println("Error running the function: ${e.message}")
}

// Subscribe to the function and print the updates
try {
    val subscribeResult = falClient.subscribe("110602490-lora", inputParams, 1000L, 30000L, true) { update ->
        println("Update: $update")
    }
    println("Final result: $subscribeResult")
} catch (e: Error) {
    println("Error subscribing to the function: ${e.message}")
}
```

## Real-time
```
import ai.fal.falclient.*

runBlocking {
    val app = "110602490-lcm-sd15-i2i"
    val authKey = "$FAL_KEY"

    val onMessage: (String) -> Unit = { message ->
        println("Received message: $message")
    }

    val onError: (Throwable) -> Unit = { error ->
        println("Error occurred: ${error.message}")
    }

    val webSocketConnection = WebSocketConnection(app, onMessage, onError)

    webSocketConnection.connect(authKey)

    val inputMap = hashMapOf(
        "prompt" to "an island near sea, with seagulls",
        "image_url" to "https://storage.googleapis.com/falserverless/model_tests/lcm/source_image.png",
        "seed" to 6252023,
        "sync_mode" to 1,
        "strength" to 0.8,
        "num_inference_steps" to 4,
    )

    while(!webSocketConnection.isConnected()) {
        delay(100)
    }

    webSocketConnection.send(Gson().toJson(inputMap))

    delay(1000)
 
    webSocketConnection.close()

}
```
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make to the Kotlin version of the client are **greatly appreciated**.

## License

Distributed under the MIT License. See [LICENSE](https://github.com/fal-ai/serverless-client-swift/blob/main/LICENSE) for more information.
