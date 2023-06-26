package io.livekit.android.sample.livestream.room.data

import retrofit2.Response
import retrofit2.http.GET

interface LivestreamApi {
    @GET("createStream")
    suspend fun createStream(creatorName: String, enableChat: Boolean, allowParticipation: Boolean): Response<CreateStreamResponse>

    @GET("joinStream")
    suspend fun joinStream(name: String, code: String): Response<JoinStreamResponse>

    @GET("inviteToStage")
    suspend fun inviteToStage(identity: String): Response<Unit>

    @GET("removeFromStage")
    suspend fun removeFromStage(identity: String): Response<Unit>

    @GET("requestToJoin")
    suspend fun requestToJoin(identity: String): Response<Unit>
}

data class CreateStreamResponse(val livekitUrl: String, val token: String)

data class JoinStreamResponse(val livekitUrl: String, val token: String)

