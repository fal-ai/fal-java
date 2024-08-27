package ai.fal.client;

public interface ApiOptions<I, O> {

    I getInput();

    String getHttpMethod();

    Class<O> getResultType();
}
