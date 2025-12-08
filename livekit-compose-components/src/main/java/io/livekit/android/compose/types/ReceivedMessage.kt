/*
 * Copyright 2025 LiveKit, Inc.
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

package io.livekit.android.compose.types

import io.livekit.android.room.participant.Participant

/**
 * A representation of a message.
 */
sealed class ReceivedMessage {
    /**
     * The id for the message.
     */
    abstract val id: String

    /**
     * The message.
     */
    abstract val message: String

    /**
     * The timestamp associated for this message,
     * measured in milliseconds since Unix epoch.
     */
    abstract val timestamp: Long

    /**
     * The participant this message belongs to.
     */
    abstract val fromParticipant: Participant?

    /**
     * A map of user defined attributes that are associated with the message.
     */
    abstract val attributes: Map<String, String>
}

/**
 * A [io.livekit.android.compose.chat.Chat] message.
 */
data class ReceivedChatMessage(
    override val id: String,
    override val message: String,
    override val timestamp: Long,
    override val fromParticipant: Participant?,
    override val attributes: Map<String, String> = emptyMap(),
    val editTimestamp: Long? = null,
) : ReceivedMessage()

/**
 * A transcription of a user's audio.
 */
data class ReceivedUserTranscriptionMessage(
    override val id: String,
    override val message: String,
    override val timestamp: Long,
    override val fromParticipant: Participant?,
    override val attributes: Map<String, String> = emptyMap(),
) : ReceivedMessage()

/**
 * A transcription of an agent's audio.
 */
data class ReceivedAgentTranscriptionMessage(
    override val id: String,
    override val message: String,
    override val timestamp: Long,
    override val fromParticipant: Participant?,
    override val attributes: Map<String, String> = emptyMap(),
) : ReceivedMessage()
