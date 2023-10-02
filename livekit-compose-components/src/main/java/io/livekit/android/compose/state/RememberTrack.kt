package io.livekit.android.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.livekit.android.compose.types.TrackIdentifier
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.compose.types.TrackSource
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication
import io.livekit.android.util.flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Observes the [trackPublication] object for the track.
 *
 * A track publication will only have the track when it is subscribed,
 * so this ensures the composition is updated with the correct track value
 * as needed.
 */
@Composable
fun <T : Track> rememberTrack(trackPublication: TrackPublication?): T? {
    val trackState = remember { mutableStateOf<T?>(null) }

    LaunchedEffect(trackPublication) {
        if (trackPublication == null) {
            trackState.value = null
        } else {
            trackPublication::track.flow.collectLatest { track ->
                @Suppress("UNCHECKED_CAST")
                trackState.value = track as? T
            }
        }
    }

    return trackState.value
}

/**
 * Observes the [trackIdentifier] object for the track.
 *
 * A track publication will only have the track when it is subscribed,
 * so this ensures the composition is updated with the correct track value
 * as needed.
 *
 * @see TrackSource
 * @see TrackReference
 */
@Composable
fun <T : Track> rememberTrack(trackIdentifier: TrackIdentifier): T? {
    return rememberTrack(trackIdentifier.getTrackPublication())
}