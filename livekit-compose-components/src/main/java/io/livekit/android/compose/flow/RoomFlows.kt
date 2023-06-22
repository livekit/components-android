package io.livekit.android.compose.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
inline fun <reified T : RoomEvent> rememberEventSelector(room: Room): State<Flow<T>> {
    val flow = remember(room) {
        mutableStateOf(MutableSharedFlow<T>(extraBufferCapacity = 100))
    }

    LaunchedEffect(room) {
        room.events.collect {
            if (it is T) {
                flow.value.emit(it)
            }
        }
    }

    return flow
}