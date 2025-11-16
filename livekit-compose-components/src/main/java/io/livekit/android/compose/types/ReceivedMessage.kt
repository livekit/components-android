package io.livekit.android.compose.types

import io.livekit.android.room.participant.Participant

sealed class ReceivedMessage {
    abstract val id: String
    abstract val message: String
    abstract val timestamp: Long
    abstract val fromParticipant: Participant?
    abstract val attributes: Map<String, String>
}

data class ReceivedChatMessage(
    override val id: String,
    override val message: String,
    override val timestamp: Long,
    override val fromParticipant: Participant?,
    override val attributes: Map<String, String> = emptyMap(),
    val editTimestamp: Long? = null,
) : ReceivedMessage()

data class ReceivedUserTranscriptionMessage(
    override val id: String,
    override val message: String,
    override val timestamp: Long,
    override val fromParticipant: Participant?,
    override val attributes: Map<String, String> = emptyMap(),
) : ReceivedMessage()

data class ReceivedAgentTranscriptionMessage(
    override val id: String,
    override val message: String,
    override val timestamp: Long,
    override val fromParticipant: Participant?,
    override val attributes: Map<String, String> = emptyMap(),
) : ReceivedMessage()