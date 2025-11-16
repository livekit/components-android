package io.livekit.android.compose.state

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.compose.test.util.withLocalContext
import io.livekit.android.room.track.TrackPublication
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.launch
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class RememberLocalMediaTest : MockE2ETest() {
    @Test
    fun basicLocalMedia() = runTest {
        connect()

        val trackPublication = TrackPublication(TestData.LOCAL_VIDEO_TRACK, mock(), room.localParticipant)
        room.localParticipant.addTrackPublication(trackPublication)

        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) {
                withLocalContext(context) {
                    rememberLocalMedia(room)
                }
            }.composeTest {
                val localMedia = awaitItem()

                assertTrue(localMedia.microphoneTrack == null)
                assertFalse(localMedia.isMicrophoneEnabled)

                assertTrue(localMedia.cameraTrack != null)
                assertTrue(localMedia.isCameraEnabled)
            }
        }

        job.join()
    }
}