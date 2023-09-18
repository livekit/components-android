/*
 * Copyright 2023 LiveKit, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Apis used for the Livestream example.
 */
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

/**
 * Apis that require an Authentication: Token <token> header
 */
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
