/*
 * Copyright 2023 LiveKit, Inc.
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

package io.livekit.android.sample.livestream.room.data

import android.content.Context
import com.twilio.audioswitch.AudioDevice
import io.livekit.android.LiveKitOverrides
import io.livekit.android.RoomOptions
import io.livekit.android.audio.AudioSwitchHandler
import io.livekit.android.room.participant.VideoTrackPublishDefaults
import io.livekit.android.room.track.VideoPreset169

fun DefaultRoomOptions(customizer: (RoomOptions) -> RoomOptions): RoomOptions {
    val roomOptions = RoomOptions(
        adaptiveStream = true,
        dynacast = true,
        videoTrackPublishDefaults = VideoTrackPublishDefaults(
            videoEncoding = VideoPreset169.HD.encoding.copy(maxBitrate = 3_000_000),
            simulcast = true,
        )
    )

    return customizer(roomOptions)
}

fun DefaultLKOverrides(context: Context) =
    LiveKitOverrides(
        audioHandler = AudioSwitchHandler(context)
            .apply {
                preferredDeviceList = listOf(
                    AudioDevice.BluetoothHeadset::class.java,
                    AudioDevice.WiredHeadset::class.java,
                    AudioDevice.Speakerphone::class.java,
                    AudioDevice.Earpiece::class.java
                )
            }
    )
