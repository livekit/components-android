package io.livekit.android.compose.flows

import androidx.compose.runtime.collectAsState
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.compose.flow.rememberEventSelector
import io.livekit.android.events.RoomEvent
import io.livekit.android.test.MockE2ETest
import io.livekit.android.test.assert.assertIsClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RememberEventSelectorTest : MockE2ETest() {

    @Test
    fun getsEvents() = runTest {
        val job = coroutineRule.scope.launch {
            moleculeFlow(RecompositionClock.Immediate) {
                rememberEventSelector<RoomEvent>(room = room).collectAsState(initial = null).value
            }.test {
                // discard initial state.
                awaitItem()
                val event = awaitItem()
                assertIsClass(RoomEvent.Connected::class.java, event)
                ensureAllEventsConsumed()
            }
        }
        connect()
        job.join()
    }
}