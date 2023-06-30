package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ParticipantMetadata(
    // true if participant requested to join stage
    val requested: Boolean = false,
    // true if participant has been invited to stage and accepted
    val isOnStage: Boolean = false,
    // true if room creator
    val isCreator: Boolean = false,
    // url of avatar
    val avatarUrl: String = "",
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun fromJson(str: String): ParticipantMetadata {
            return Json.decodeFromString(str)
        }
    }
}