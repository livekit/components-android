package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.Serializable


@Serializable
data class RoomMetadata(
    val livestream: LivestreamInfo
)