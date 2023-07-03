package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LivestreamApi {
    @POST("/api/create_stream")
    suspend fun createStream(
        @Body body: CreateStreamRequest
    ): Response<CreateStreamResponse>

    @POST("/api/join_stream")
    suspend fun joinStream(
        @Body body: JoinStreamRequest
    ): Response<JoinStreamResponse>
}

interface AuthenticatedLivestreamApi {

    @POST("/api/invite_to_stage")
    suspend fun inviteToStage(
        @Body body: IdentityRequest
    ): Response<Unit>

    @POST("/api/remove_from_stage")
    suspend fun removeFromStage(
        @Body body: IdentityRequest
    ): Response<Unit>

    @POST("/api/raise_hand")
    suspend fun requestToJoin(): Response<Unit>

    @POST("/api/stop_stream")
    suspend fun stopStream(): Response<Unit>
}

@Serializable
data class CreateStreamRequest(
    @SerialName("room_name")
    val roomName: String,
    val metadata: RoomMetadata,
)

@Serializable
data class CreateStreamResponse(
    @SerialName("auth_token")
    val authToken: String,
    @SerialName("connection_details")
    val connectionDetails: ConnectionDetails
)

@Serializable
data class JoinStreamRequest(
    @SerialName("room_name")
    val roomName: String,
    val identity: String,
)

@Serializable
data class JoinStreamResponse(
    @SerialName("auth_token")
    val authToken: String,
    @SerialName("connection_details")
    val connectionDetails: ConnectionDetails
)

@Serializable
data class ConnectionDetails(
    @SerialName("ws_url")
    val wsUrl: String,
    val token: String,
)

@Serializable
data class IdentityRequest(
    val identity: String
)