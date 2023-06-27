package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LivestreamApi {
    @GET("createStream")
    suspend fun createStream(
        @Query("creatorName") creatorName: String,
        @Query("roomName") roomName: String,
        @Query("enableChat") enableChat: Boolean,
        @Query("allowParticipation") allowParticipation: Boolean
    ): Response<CreateStreamResponse>

    @GET("joinStream")
    suspend fun joinStream(
        @Query("name") name: String,
        @Query("code") code: String
    ): Response<JoinStreamResponse>

    @GET("inviteToStage")
    suspend fun inviteToStage(
        @Query("identity") identity: String
    ): Response<Unit>

    @GET("removeFromStage")
    suspend fun removeFromStage(
        @Query("identity") identity: String
    ): Response<Unit>

    @GET("requestToJoin")
    suspend fun requestToJoin(
        @Query("identity") identity: String
    ): Response<Unit>
}

@Serializable
data class CreateStreamResponse(val livekitUrl: String, val token: String)

@Serializable
data class JoinStreamResponse(val livekitUrl: String, val token: String)

