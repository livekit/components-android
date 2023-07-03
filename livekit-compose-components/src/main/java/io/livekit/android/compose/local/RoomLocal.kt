package io.livekit.android.compose.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.RoomOptions
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.util.flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RoomState(val room: Room) {
    val participants = mutableStateOf(listOf<Participant>(room.localParticipant).plus(room.remoteParticipants.values))

    val metadata = mutableStateOf(room.metadata)

    val connectionState = mutableStateOf(room.state)
}

@Composable
fun rememberLiveKitRoom(
    url: String? = null,
    token: String? = null,
    audio: Boolean = false,
    video: Boolean = false,
    connect: Boolean = true,
    roomOptions: RoomOptions? = null,
    liveKitOverrides: LiveKitOverrides? = null,
    connectOptions: ConnectOptions? = null,
    onConnected: (suspend CoroutineScope.(Room) -> Unit)? = null,
    onDisconnected: (suspend CoroutineScope.(Room) -> Unit)? = null,
    onError: ((Exception?) -> Unit)? = null,
    passedRoom: Room? = null,
): Room {
    val context = LocalContext.current
    val room = remember(passedRoom) {
        passedRoom ?: LiveKit.create(
            appContext = context.applicationContext,
            options = roomOptions ?: RoomOptions(),
            overrides = liveKitOverrides ?: LiveKitOverrides(),
        )
    }

    LaunchedEffect(room, onConnected, onDisconnected, onError) {
        launch {
            room::state.flow.collectLatest { state ->
                when (state) {
                    Room.State.CONNECTED -> {
                        if (audio) {
                            room.localParticipant.setMicrophoneEnabled(true)
                        }
                        if (video) {
                            room.localParticipant.setCameraEnabled(true)
                        }
                        onConnected?.invoke(this, room)
                    }

                    Room.State.DISCONNECTED -> {
                        onDisconnected?.invoke(this, room)
                    }

                    else -> {
                        /* do nothing */
                    }
                }
            }
        }
    }
    LaunchedEffect(connect, url, token, room, connectOptions, onError) {
        if (url.isNullOrEmpty() || token.isNullOrEmpty()) {
            return@LaunchedEffect
        }

        if (connect) {
            try {
                room.connect(url, token, connectOptions ?: ConnectOptions())
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }

    DisposableEffect(room, connect) {
        onDispose {
            if (connect) {
                room.disconnect()
            }
        }
    }
    return room
}

@Composable
fun RoomScope(
    url: String? = null,
    token: String? = null,
    audio: Boolean = false,
    video: Boolean = false,
    connect: Boolean = true,
    roomOptions: RoomOptions? = null,
    liveKitOverrides: LiveKitOverrides? = null,
    connectOptions: ConnectOptions? = null,
    onConnected: (suspend CoroutineScope.(Room) -> Unit)? = null,
    onDisconnected: (suspend CoroutineScope.(Room) -> Unit)? = null,
    onError: ((Exception?) -> Unit)? = null,
    passedRoom: Room? = null,
    content: @Composable () -> Unit
) {
    val room = rememberLiveKitRoom(
        url = url,
        token = token,
        audio = audio,
        video = video,
        connect = connect,
        roomOptions = roomOptions,
        liveKitOverrides = liveKitOverrides,
        connectOptions = connectOptions,
        onConnected = onConnected,
        onDisconnected = onDisconnected,
        onError = onError,
        passedRoom = passedRoom
    )

    CompositionLocalProvider(
        RoomLocal provides room,
        content = content,
    )
}

@Composable
@Throws(IllegalStateException::class)
fun requireRoom(passedRoom: Room? = null): Room {
    return passedRoom ?: RoomLocal.current
}

val RoomLocal =
    compositionLocalOf<Room> { throw IllegalStateException("No Room object available. This should only be used within a RoomScope.") }