package io.livekit.android.sample.livestream.room.data

import android.content.Context
import com.twilio.audioswitch.AudioDevice
import io.livekit.android.LiveKitOverrides
import io.livekit.android.RoomOptions
import io.livekit.android.audio.AudioSwitchHandler

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