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