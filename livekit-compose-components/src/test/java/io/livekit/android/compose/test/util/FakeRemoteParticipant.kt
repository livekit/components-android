/*
 * Copyright 2025 LiveKit, Inc.
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

package io.livekit.android.compose.test.util

import io.livekit.android.room.SignalClient
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.RemoteAudioTrack
import io.livekit.android.room.track.RemoteVideoTrack
import io.livekit.android.test.mock.MockRTCThreadToken
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import livekit.org.webrtc.AudioTrack
import livekit.org.webrtc.RtpReceiver
import livekit.org.webrtc.VideoTrack
import org.mockito.Mockito

fun createFakeRemoteParticipant(dispatcher: CoroutineDispatcher): RemoteParticipant {
    return RemoteParticipant(
        info = TestData.REMOTE_PARTICIPANT,
        signalClient = Mockito.mock(SignalClient::class.java),
        ioDispatcher = Dispatchers.IO,
        defaultDispatcher = Dispatchers.Default,
        audioTrackFactory = object : RemoteAudioTrack.Factory {
            override fun create(
                name: String,
                rtcTrack: AudioTrack,
                receiver: RtpReceiver
            ): RemoteAudioTrack {
                return RemoteAudioTrack(
                    name = name,
                    rtcTrack = rtcTrack,
                    receiver = receiver,
                    rtcThreadToken = MockRTCThreadToken()
                )
            }
        },
        videoTrackFactory = object : RemoteVideoTrack.Factory {
            override fun create(name: String, rtcTrack: VideoTrack, autoManageVideo: Boolean, receiver: RtpReceiver): RemoteVideoTrack {
                return RemoteVideoTrack(
                    name = name,
                    rtcTrack = rtcTrack,
                    autoManageVideo = autoManageVideo,
                    dispatcher = dispatcher,
                    receiver = receiver,
                    rtcThreadToken = MockRTCThreadToken()
                )
            }
        }
    ).apply {
        updateFromInfo(TestData.REMOTE_PARTICIPANT)
    }
}
