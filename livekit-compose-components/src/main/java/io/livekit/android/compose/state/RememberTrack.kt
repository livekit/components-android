/*
 * Copyright 2024 LiveKit, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
internal fun <T : Track> rememberTrack(trackPublication: TrackPublication?): T? {
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
    return rememberTrack(trackIdentifier.publication)
}
