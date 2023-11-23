package io.livekit.android.compose.local

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import io.livekit.android.compose.types.TrackReference


@SuppressLint("CompositionLocalNaming")
val TrackReferenceLocal =
    compositionLocalOf<TrackReference> { throw IllegalStateException("No Track object available. This should only be used within a TrackReferenceScope.") }

/**
 * Binds [trackRef] to the [TrackReferenceLocal] for the scope of [content].
 */
@Composable
fun TrackReferenceScope(
    trackRef: TrackReference,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        TrackReferenceLocal provides trackRef,
        content = content,
    )
}

/**
 * Returns the [passedTrack] or the currently provided [TrackReferenceLocal].
 * @throws IllegalStateException if passedTrack is null and no TrackReferenceLocal is available (e.g. not inside a [TrackReferenceScope]).
 */
@Composable
@Throws(IllegalStateException::class)
fun requireTrack(passedTrack: TrackReference? = null): TrackReference {
    return passedTrack ?: TrackReferenceLocal.current
}

/**
 * A simple way to loop over tracks that creates a [TrackReferenceScope] for each track and calls [content].
 */
@Composable
fun ForEachTrack(
    tracks: List<TrackReference>,
    content: @Composable (TrackReference) -> Unit
) {
    tracks.forEach { trackRef ->
        TrackReferenceScope(trackRef = trackRef) {
            content(trackRef)
        }
    }
}