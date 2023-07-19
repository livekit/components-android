package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ParticipantMetadata(
    // true if participant requested to join stage
    @SerialName("hand_raised")
    val handRaised: Boolean = false,
    /**
     * true if participant has been invited to stage by the host
     */
    @SerialName("invited_to_stage")
    val invitedToStage: Boolean = false,
) {

    /**
     * true if the participant is both handRaised and invited to stage.
     */
    val isOnStage
        get() = handRaised && invitedToStage

    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun fromJson(str: String): ParticipantMetadata {
            return Json.decodeFromString(str)
        }
    }
}