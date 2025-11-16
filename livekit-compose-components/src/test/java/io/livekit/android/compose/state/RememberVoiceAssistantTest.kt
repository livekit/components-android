/*
 * Copyright 2024-2025 LiveKit, Inc.
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
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.flow.TextStreamData
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.room.track.Track
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.MockAudioStreamTrack
import io.livekit.android.test.mock.MockRtpReceiver
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, Beta::class)
class RememberVoiceAssistantTest : MockE2ETest() {

    @Test
    fun initialState() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            rememberVoiceAssistant(room)
        }.composeTest {
            val voiceAssistant = awaitItem()

            assertEquals(null, voiceAssistant.agent)
            assertEquals(AgentState.DISCONNECTED, voiceAssistant.state)
            assertEquals(null, voiceAssistant.audioTrack)
            assertEquals(emptyList<TextStreamData>(), voiceAssistant.agentTranscriptions)
            assertEquals(emptyMap<String, String>(), voiceAssistant.agentAttributes)
        }
    }

    suspend fun <T> agentJoinTest(body: @Composable () -> T, validate: suspend TurbineTestContext<T>.() -> Unit) {
        val testJob = coroutineRule.scope.launch {
            moleculeFlow(RecompositionMode.Immediate) { body() }
                .distinctUntilChanged()
                .test {
                    validate()
                    delay(1)
                }
            println("job done")
        }

        connect()
        val agentJoin = TestData.AGENT_JOIN
        simulateMessageFromServer(agentJoin)

        room.remoteParticipants.values.first().addSubscribedMediaTrack(
            mediaTrack = MockAudioStreamTrack(),
            sid = TestData.REMOTE_AUDIO_TRACK.sid,
            statsGetter = {},
            receiver = MockRtpReceiver.create(),
            autoManageVideo = false,
            triesLeft = 1
        )

        println("await job")
        testJob.join()

        println("finish await job")
    }

    @Test
    fun agentJoin() = runTest {
        agentJoinTest(
            body = {
                rememberVoiceAssistant(room).agent
            },
            validate = {
                assertNull(awaitItem())
                val agent = awaitItem()
                val remoteParticipant = room.remoteParticipants.values.first()

                assertEquals(
                    remoteParticipant,
                    agent,
                )
            }
        )
    }

    @Test
    fun agentJoinAudioTrack() = runTest {
        agentJoinTest(
            body = {
                rememberVoiceAssistant(room).audioTrack
            },
            validate = {
                assertNull(awaitItem())

                val audioTrack = awaitItem()
                val agent = room.remoteParticipants.values.first()

                assertEquals(
                    TrackReference(
                        participant = agent,
                        publication = agent.audioTrackPublications.first().first,
                        source = Track.Source.MICROPHONE,
                    ),
                    audioTrack,
                )
            }
        )
    }

    @Test
    fun agentJoinState() = runTest {
        agentJoinTest(
            body = {
                rememberVoiceAssistant(room).state
            },
            validate = {
                assertEquals(AgentState.DISCONNECTED, awaitItem())
                assertEquals(AgentState.CONNECTING, awaitItem())
                assertEquals(AgentState.INITIALIZING, awaitItem())
                assertEquals(AgentState.LISTENING, awaitItem())
            }
        )
    }

    @Test
    fun agentJoinTranscriptions() = runTest {
        agentJoinTest(
            body = {
                rememberVoiceAssistant(room).agentTranscriptions
            },
            validate = {
                assertEquals(emptyList<TextStreamData>(), awaitItem())
            }
        )
    }

    @Test
    fun agentJoinAttributes() = runTest {
        agentJoinTest(
            body = {
                rememberVoiceAssistant(room).agentAttributes
            },
            validate = {
                assertEquals(emptyMap<String, String>(), awaitItem())
                assertEquals(mapOf(PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_KEY to PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_LISTENING), awaitItem())
            }
        )
    }
}
