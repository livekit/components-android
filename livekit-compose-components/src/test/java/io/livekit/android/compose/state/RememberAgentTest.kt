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
    fun agentStateWithPreconnect() = runTest {
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
                    assertEquals(AgentState.LISTENING, awaitItem())
                    assertEquals(AgentState.DISCONNECTED, awaitItem())
                }
        }

        sessionConnect()

        job.join()
    }
}