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

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.types.TrackReference
import io.livekit.android.room.track.Track
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.MockAudioStreamTrack
import io.livekit.android.test.mock.MockRtpReceiver
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import livekit.LivekitModels.ParticipantInfo.Kind
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, Beta::class)
class RememberVoiceAssistantTest : MockE2ETest() {

    @Test
    fun initialState() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            rememberVoiceAssistant(room)
        }.test {
            assertEquals(
                VoiceAssistant(
                    agent = null,
                    state = AgentState.DISCONNECTED,
                    audioTrack = null,
                    agentTranscriptions = listOf(),
                    agentAttributes = mapOf(),
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun agentJoin() = runTest {
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberVoiceAssistant(room)
            }.test {
                awaitItem() // intermediate flow emissions, not under test.
                awaitItem()
                awaitItem()
                awaitItem()
                awaitItem()

                val agent = room.remoteParticipants.values.first()
                assertEquals(
                    VoiceAssistant(
                        agent = agent,
                        state = AgentState.LISTENING,
                        audioTrack = TrackReference(
                            participant = agent,
                            publication = agent.audioTrackPublications.first().first,
                            source = Track.Source.MICROPHONE,
                        ),
                        agentTranscriptions = listOf(),
                        agentAttributes = mapOf(PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_KEY to PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_LISTENING),
                    ),
                    awaitItem()
                )
            }
        }

        connect()

        val agentJoin = with(TestData.PARTICIPANT_JOIN.toBuilder()) {
            update = with(update.toBuilder()) {
                clearParticipants()
                val agent = with(TestData.REMOTE_PARTICIPANT.toBuilder()) {
                    kind = Kind.AGENT
                    clearAttributes()
                    putAttributes(PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_KEY, PARTICIPANT_ATTRIBUTE_LK_AGENT_STATE_LISTENING)
                    build()
                }
                addParticipants(agent)
                build()
            }
            build()
        }
        simulateMessageFromServer(agentJoin)

        room.remoteParticipants.values.first().addSubscribedMediaTrack(
            mediaTrack = MockAudioStreamTrack(),
            sid = TestData.REMOTE_AUDIO_TRACK.sid,
            statsGetter = {},
            receiver = MockRtpReceiver.create(),
            autoManageVideo = false,
            triesLeft = 1
        )

        job.join()
    }
}
