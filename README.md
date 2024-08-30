## The fal.ai Kotlin Client

![License](https://img.shields.io/badge/license-MIT-blue)

## About the Project

The `FalClient` is a robust and user-friendly Java implementation of the [fal.ai](https://fal.ai) client.

## Getting Started

The `FalClient` library serves as a client for fal serverless Python functions. Before using this library, ensure you've got your fal key from [our dashboard](https://fal.ai/dashboard/keys).

The client is available on Maven Central. There are three different modules:

- `fal-client`: The main client library, implemented in Java, with synchronous interfaces.
- `fal-client-async`: The asynchronous version of the client library, implemented in Java.
- `fal-client-kotlin`: The Kotlin version of the client library, with coroutines support, implemented on top of the `fal-client-async` module.

The

### Client Library


#### Synchronous

##### Install

```groovy
implementation "ai.fal.client:fal-client:0.7.0"
```

##### Call the API

```java
import ai.fal.client.*;

var fal = FalClient.withEnvCredentials();

var input = JsonInput.input()
    .set("prompt", "A cute shih-tzu puppy")
    .build();
var result = fal.subscribe("fal-ai/fast-sdxl", SubscribeOptions.withInput(input));

System.out.println(result.getRequestId());
System.out.println(result.getData());
```

#### Asynchronous

##### Install

```groovy
implementation "ai.fal.client:fal-client-async:0.7.0"
```

##### Call the API

```java
import ai.fal.client.*;

var fal = FalAsyncClient.withEnvCredentials();

var input = JsonInput.input()
    .set("prompt", "A cute shih-tzu puppy")
    .build();
CompletableFuture<> result = fal.subscribe("fal-ai/fast-sdxl", SubscribeOptions.withInput(input));

result.thenAccept(result -> {
    System.out.println(result.getRequestId());
    System.out.println(result.getData());
});
```

#### Kotlin

##### Install

```groovy
implementation "ai.fal.client:fal-client-kotlin:0.7.0"
```

##### Call the API

```kotlin
import ai.fal.client.kt.*

val fal = createFalClient()

val input = mapOf(
    "prompt" to "A cute shih-tzu puppy"
)
val result = fal.subscribe("fal-ai/fast-sdxl", input) { update ->
    if (update is QueueUpdate.InProgress) {
        println(update.logs)
    }
}
```

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make to the Kotlin version of the client are **greatly appreciated**.

## License

Distributed under the MIT License. See [LICENSE](https://github.com/fal-ai/serverless-client-swift/blob/main/LICENSE) for more information.
