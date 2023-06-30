package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class LivestreamInfo(
    // Name of the room
    val code: String = "",
    // Url to join the room
    val url: String = "",
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun fromJson(str: String): LivestreamInfo {
            return Json.decodeFromString(str)
        }
    }
}