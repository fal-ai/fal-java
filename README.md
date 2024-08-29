## The fal.ai Kotlin Client

![License](https://img.shields.io/badge/license-MIT-blue)

## About the Project

The `FalClient` is a robust and user-friendly Java implementation of the fal.ai client.

## Getting Started

The `FalClient` library serves as a client for fal serverless Python functions. Before using this library, ensure you've got your fal key from [our dashboard](https://fal.ai/dashboard/keys).

### Install

Add `ai.fal:fal-client` dependency to your build file. Here is the `build.gradle` example:

```groovy
implementation "ai.fal:fal-client:0.2.0"
```

### Client Library

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

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make to the Kotlin version of the client are **greatly appreciated**.

## License

Distributed under the MIT License. See [LICENSE](https://github.com/fal-ai/serverless-client-swift/blob/main/LICENSE) for more information.
