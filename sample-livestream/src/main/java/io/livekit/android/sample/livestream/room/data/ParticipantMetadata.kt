package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.Serializable

@Serializable
data class ParticipantMetadata(
    // true if participant requested to join stage
    val requested: Boolean,
    // true if participant has been invited to stage and accepted
    val isOnStage: Boolean,
    // true if room creator
    val isCreator: Boolean,
    // url of avatar
    val avatarUrl: String,
)