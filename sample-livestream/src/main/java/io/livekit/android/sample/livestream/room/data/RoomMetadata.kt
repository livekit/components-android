package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString


@Serializable
data class RoomMetadata(
    @SerialName("creator_identity")
    val creatorIdentity: String,
    @SerialName("enable_chat")
    val enableChat: Boolean,
    @SerialName("allow_participation")
    val allowParticipation: Boolean
) {
    fun toJson(): String {
        return LKJson.encodeToString(this)
    }

    companion object {
        fun fromJson(str: String): RoomMetadata {
            return LKJson.decodeFromString(str)
        }
    }
}