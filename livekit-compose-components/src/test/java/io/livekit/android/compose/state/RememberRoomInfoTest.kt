package io.livekit.android.compose.state

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.mock.TestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RememberRoomInfoTest : MockE2ETest() {

    @Test
    fun initialState() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            rememberRoomInfo(room)
        }.test {
            val info = awaitItem()
            assertEquals(room.name, info.name)
            assertEquals(room.metadata, info.metadata)
        }
    }

    @Test
    fun getRoomInfoChanges() = runTest {
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberRoomInfo(room)
            }.test {
                delay(100)

                val info = expectMostRecentItem()
                val expectedRoom = TestData.JOIN.join.room
                assertEquals(expectedRoom.name, info.name)
                assertEquals(expectedRoom.metadata, info.metadata)
            }
        }
        connect()
        job.join()
    }
}