package io.livekit.android.compose.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.livekit.android.room.Room
import io.livekit.android.test.MockE2ETest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomScopeTest : MockE2ETest() {

    @Test
    fun passesBackRoom() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            var testRoom: Room? = null
            RoomScopeSetup {
                RoomScope(connect = false, passedRoom = room) {
                    testRoom = it
                }
            }
            testRoom
        }.test {
            val retRoom = awaitItem()
            assertNotNull(retRoom)
        }
    }

    @Test
    fun roomCreateIfNotPassed() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            var testRoom: Room? = null
            RoomScopeSetup {
                RoomScope(connect = false) {
                    testRoom = it
                }
            }
            testRoom
        }.test {
            awaitError() // real rooms can't be created in test env.
        }
    }

    /**
     * RoomScope requires a LocalContext, which would normally be
     * provided in a ComposeView.
     */
    @Composable
    fun RoomScopeSetup(content: @Composable () -> Unit) {
        CompositionLocalProvider(
            LocalContext provides context
        ) {
            content()
        }
    }
}