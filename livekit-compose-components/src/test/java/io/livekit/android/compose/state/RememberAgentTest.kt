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

import androidx.compose.runtime.LaunchedEffect
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.test.util.composeTest
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.TestData
import io.livekit.android.token.TokenSource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(Beta::class)
class RememberAgentTest : MockE2ETest() {
    @Test
    fun basicSession() = runTest {
        val job = launch {
            moleculeFlow(RecompositionMode.Immediate) {
                val session = rememberSession(
                    tokenSource = TokenSource.fromLiteral(TestData.EXAMPLE_URL, "token"),
                    options = SessionOptions(
                        room = room
                    )
                )

                val agent = rememberAgent(session)

                LaunchedEffect(Unit) {
                    val result = session.start()
                    assertTrue(result.isSuccess)

                    val agentJoin = TestData.AGENT_JOIN
                    simulateMessageFromServer(agentJoin)

                    session.end()
                }
                agent.agentParticipant
            }.distinctUntilChanged().composeTest {
                assertEquals(null, awaitItem())
                assertEquals(TestData.REMOTE_PARTICIPANT.identity, awaitItem()?.identity?.value)
                assertEquals(null, awaitItem())
            }
        }

        sessionConnect()

        job.join()
    }

    @Test
    fun agentStateWithoutPreconnect() = runTest {
        val job = launch {
            moleculeFlow(RecompositionMode.Immediate) {
                val session = rememberSession(
                    tokenSource = TokenSource.fromLiteral(TestData.EXAMPLE_URL, "token"),
                    options = SessionOptions(
                        room = room,
                    )
                )

                val agent = rememberAgent(session)

                LaunchedEffect(Unit) {
                    val startResult = session.start(
                        options = SessionConnectOptions(
                            tracks = SessionConnectTrackOptions(
                                microphoneEnabled = false,
                                usePreconnectBuffer = false,
                            )
                        )
                    )
                    assertTrue(startResult.isSuccess)

                    val agentJoin = TestData.AGENT_JOIN
                    simulateMessageFromServer(agentJoin)

                    session.end()
                }
                agent.agentState
            }
                .distinctUntilChanged()
                .composeTest {
                    assertEquals(AgentState.DISCONNECTED, awaitItem())
                    assertEquals(AgentState.CONNECTING, awaitItem())
                    assertEquals(AgentState.LISTENING, awaitItem())
                    assertEquals(AgentState.DISCONNECTED, awaitItem())
                }
        }

        sessionConnect()

        job.join()
    }

    @Test
    fun agentStateWithPreconnect() = runTest {
        val job = launch {
            moleculeFlow(RecompositionMode.Immediate) {
                val session = rememberSession(
                    tokenSource = TokenSource.fromLiteral(TestData.EXAMPLE_URL, "token"),
                    options = SessionOptions(
                        room = room,
                    )
                )

                val agent = rememberAgent(session)

                LaunchedEffect(Unit) {
                    assertTrue(session.start().isSuccess)

                    val agentJoin = TestData.AGENT_JOIN
                    simulateMessageFromServer(agentJoin)

                    session.end()
                }
                agent.agentState
            }
                .distinctUntilChanged()
                .composeTest {
                    assertEquals(AgentState.DISCONNECTED, awaitItem())
                    assertEquals(AgentState.CONNECTING, awaitItem())
                    assertEquals(AgentState.PRECONNECT_BUFFERING, awaitItem())
                    assertEquals(AgentState.LISTENING, awaitItem())
                    assertEquals(AgentState.DISCONNECTED, awaitItem())
                }
        }

        sessionConnect()

        job.join()
    }

    @Test
    fun agentTimeoutWithPreconnect() = runTest {
        val job = launch {
            moleculeFlow(RecompositionMode.Immediate) {
                val session = rememberSession(
                    tokenSource = TokenSource.fromLiteral(TestData.EXAMPLE_URL, "token"),
                    options = SessionOptions(
                        room = room,
                        agentConnectTimeout = 100.milliseconds
                    )
                )

                val agent = rememberAgent(session)

                LaunchedEffect(Unit) {
                    val startResult = session.start()
                    assertTrue(startResult.isSuccess)
                }
                agent.agentState
            }
                .distinctUntilChanged()
                .composeTest {
                    assertEquals(AgentState.DISCONNECTED, awaitItem())
                    assertEquals(AgentState.CONNECTING, awaitItem())
                    assertEquals(AgentState.PRECONNECT_BUFFERING, awaitItem())
                    assertEquals(AgentState.FAILED, awaitItem())
                }
        }

        sessionConnect()

        job.join()
    }

    @Test
    fun agentTimeoutWithoutPreconnect() = runTest {
        val job = launch {
            moleculeFlow(RecompositionMode.Immediate) {
                val session = rememberSession(
                    tokenSource = TokenSource.fromLiteral(TestData.EXAMPLE_URL, "token"),
                    options = SessionOptions(
                        room = room,
                        agentConnectTimeout = 100.milliseconds
                    )
                )

                val agent = rememberAgent(session)

                LaunchedEffect(Unit) {
                    val startResult = session.start(
                        options = SessionConnectOptions(
                            tracks = SessionConnectTrackOptions(
                                microphoneEnabled = false,
                                usePreconnectBuffer = false,
                            )
                        )
                    )
                    assertTrue(startResult.isSuccess)
                }
                agent.agentState
            }
                .distinctUntilChanged()
                .composeTest {
                    assertEquals(AgentState.DISCONNECTED, awaitItem())
                    assertEquals(AgentState.CONNECTING, awaitItem())
                    assertEquals(AgentState.FAILED, awaitItem())
                }
        }

        sessionConnect()

        job.join()
    }
}
