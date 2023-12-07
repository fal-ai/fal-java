package ai.fal.falclient

import retrofit2.Response
import retrofit2.http.*

interface FalService {

    @POST
    suspend fun run(
        @Url url: String,
        @Body input: HashMap<String, String>,
        @QueryMap options: Map<String, String>
    ): Response<HashMap<String, Any>>

    @POST
    suspend fun submitToQueue(
        @Url url: String,
        @Body input: HashMap<String, Any>?
    ): Response<Map<String, String>>

    @GET
    suspend fun getQueueStatus(
        @Url url: String,
        @Query("includeLogs") includeLogs: Boolean
    ): Response<HashMap<String, Any>>

    @GET
    suspend fun getQueueResponse(
        @Url url: String
    ): Response<HashMap<String, Any>>
}
