## The fal.ai JVM Client for Java and Kotlin

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
implementation "ai.fal.client:fal-client:0.7.1"
```

##### Call the API

```java
import ai.fal.client.*;

var fal = FalClient.withEnvCredentials();

var input = Map.of(
    "prompt", "A cute shih-tzu puppy"
);
var result = fal.subscribe("fal-ai/fast-sdxl",
    SubscribeOptions.<JsonObject>builder()
        .input(input)
        .resultType(JsonObject.class)
        .onQueueUpdate(update -> {
            System.out.println(update.getStatus());
        })
        .build()
);
System.out.println(result.getRequestId());
System.out.println(result.getData());
```

#### Asynchronous

##### Install

```groovy
implementation "ai.fal.client:fal-client-async:0.7.1"
```

##### Call the API

```java
import ai.fal.client.*;

var fal = AsyncFalClient.withEnvCredentials();

var input = Map.of(
    "prompt", "A cute shih-tzu puppy"
);
var future = fal.subscribe("fal-ai/fast-sdxl",
    SubscribeOptions.<JsonObject>builder()
        .input(input)
        .resultType(JsonObject.class)
        .onQueueUpdate(update -> {
            System.out.println(update.getStatus());
        })
        .build()
);
future.thenAccept(result -> {
    System.out.println(result.getRequestId());
    System.out.println(result.getData());
});
```

#### Kotlin

##### Install

```groovy
implementation "ai.fal.client:fal-client-kotlin:0.7.1"
```

##### Call the API

```kotlin
import ai.fal.client.kt.*

val fal = createFalClient()

val result = fal.subscribe("fal-ai/fast-sdxl", input = mapOf(
    "prompt" to "A cute shih-tzu puppy"
)) { update ->
    print(update.status)
}
print(result.requestId)
print(result.data)
```

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make to the Kotlin version of the client are **greatly appreciated**.

## License

Distributed under the MIT License. See [LICENSE](https://github.com/fal-ai/serverless-client-swift/blob/main/LICENSE) for more information.
