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

package io.livekit.android.compose.state

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import io.livekit.android.annotations.Beta
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

@OptIn(Beta::class)
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
