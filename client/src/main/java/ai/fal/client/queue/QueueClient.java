package ai.fal.client.queue;

import ai.fal.client.Result;
import jakarta.annotation.Nonnull;

/** A client for interacting with the queue endpoints. */
public interface QueueClient {

    /**
     * Submit a payload to an endpoint's queue.
     *
     * @param <I> the type of the input payload
     * @param endpointId the endpoint to submit to (e.g. `fal-ai/fast-sdxl`)
     * @param options the submit options
     * @return the status of the submission with the `requestId` for tracking the submission.
     */
    @Nonnull
    <I> QueueStatus.InQueue submit(String endpointId, QueueSubmitOptions<I> options);

    /**
     * Check the status of a submission.
     *
     * @param endpointId the endpoint to cancel the submission for
     * @param options the status check options
     * @return the status of the submission
     */
    @Nonnull
    QueueStatus.StatusUpdate status(@Nonnull String endpointId, @Nonnull QueueStatusOptions options);

    /**
     * Get the result of a submission.
     *
     * @param <O> the type of the output payload
     * @param endpointId the endpoint to get the result for
     * @param options the response options
     * @return the result of the submission
     */
    @Nonnull
    <O> Result<O> result(@Nonnull String endpointId, @Nonnull QueueResponseOptions<O> options);
}
