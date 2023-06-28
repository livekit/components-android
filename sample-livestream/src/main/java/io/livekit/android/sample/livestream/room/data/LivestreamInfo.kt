package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.Serializable

@Serializable
data class LivestreamInfo(
    // Name of the room
    val code: String = "",
    // Url to join the room
    val url: String = "",
)