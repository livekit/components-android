package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
data class RoomMetadata(
    val livestream: LivestreamInfo
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun fromJson(str: String): RoomMetadata {
            return Json.decodeFromString(str)
        }
    }
}