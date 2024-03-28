/*
 * Copyright 2023 LiveKit, Inc.
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

package io.livekit.android.compose.local

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.RoomOptions
import io.livekit.android.room.Room
import io.livekit.android.room.RoomException
import io.livekit.android.util.flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple handler for listening to room state changes.
 * @param states the types of states to listen to, or empty list to listen to all state changes.
 * @param passedRoom the room to use, or null to use [RoomLocal] if inside a [RoomScope].
 * @param onState the listener to be called back. Will be called immediately with the existing state if it is a matching state.
 * @param keys any keys that should be tracked to relaunch the handler with new keys.
 */
@Composable
fun HandleRoomState(
    states: List<Room.State> = emptyList(),
    passedRoom: Room? = null,
    vararg keys: Any,
    onState: (suspend CoroutineScope.(Room, Room.State) -> Unit)?,
) {
    val room = requireRoom(passedRoom = passedRoom)

    if (onState != null) {
        val effectKeys = listOf(room, onState).plus(keys).toTypedArray()
        LaunchedEffect(*effectKeys) {
            launch {
                room::state.flow.collectLatest { currentState ->
                    if (states.isEmpty() || states.contains(currentState)) {
                        onState.invoke(this, room, currentState)
                    }
                }
            }
        }
    }
}

/**
 * @see HandleRoomState
 */
@Composable
fun HandleRoomState(
    state: Room.State,
    passedRoom: Room? = null,
    vararg keys: Any,
    onState: (suspend CoroutineScope.(Room, Room.State) -> Unit)?
) {
    val states = listOf(state)
    HandleRoomState(
        states = states,
        passedRoom = passedRoom,
        keys = keys,
        onState = onState
    )
}

/**
 * Remembers a new [Room] object.
 *
 * @param url the url of the livekit server to connect to.
 * @param token the token to connect to livekit with.
 * @param audio enable or disable audio. Defaults to false.
 * @param video enable or disable video. Defaults to false.
 * @param connect whether the room should automatically connect to the server. Defaults to true.
 * @param roomOptions options to pass to the [Room].
 * @param liveKitOverrides overrides to pass to the [Room].
 * @param connectOptions options to use when connecting. Will not reflect changes if already connected.
 * @param onConnected a listener to be called upon room connection.
 * @param onDisconnected a listener to be called upon room disconnection.
 * @param onError a listener to be called upon room error.
 * @param passedRoom if a [Room] is provided, it will be used. If null, a new Room will be created instead.
 */
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
    onError: ((Room, Exception?) -> Unit)? = null,
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

    DisposableEffect(roomOptions) {
        roomOptions?.audioTrackCaptureDefaults?.let {
            room.audioTrackCaptureDefaults = it
        }
        roomOptions?.videoTrackCaptureDefaults?.let {
            room.videoTrackCaptureDefaults = it
        }

        roomOptions?.audioTrackPublishDefaults?.let {
            room.audioTrackPublishDefaults = it
        }
        roomOptions?.videoTrackPublishDefaults?.let {
            room.videoTrackPublishDefaults = it
        }
        roomOptions?.adaptiveStream?.let {
            room.adaptiveStream = it
        }
        roomOptions?.e2eeOptions?.let {
            room.e2eeOptions = it
        }
        onDispose { }
    }
    HandleRoomState(Room.State.CONNECTED, room) { _, _ -> onConnected?.invoke(this, room) }
    HandleRoomState(Room.State.CONNECTED, room, audio) { _, _ ->
        room.localParticipant.setMicrophoneEnabled(audio)
    }
    HandleRoomState(Room.State.CONNECTED, room, video) { _, _ ->
        room.localParticipant.setCameraEnabled(video)
    }
    HandleRoomState(Room.State.DISCONNECTED, room) { _, _ -> onDisconnected?.invoke(this, room) }

    LaunchedEffect(room, connect, url, token, connectOptions) {
        if (url.isNullOrEmpty() || token.isNullOrEmpty()) {
            return@LaunchedEffect
        }

        if (connect) {
            try {
                room.connect(url, token, connectOptions ?: ConnectOptions())
            } catch (e: Exception) {
                onError?.invoke(room, RoomException.ConnectException(e.message, e))
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

/**
 * Establishes a room scope which remembers a [Room] object which can be accessed
 * through the [RoomLocal] composition local.
 *
 * @param url the url of the livekit server to connect to.
 * @param token the token to connect to livekit with.
 * @param audio enable or disable audio. Defaults to false.
 * @param video enable or disable video. Defaults to false.
 * @param connect whether the room should automatically connect to the server. Defaults to true.
 * @param roomOptions options to pass to the [Room].
 * @param liveKitOverrides overrides to pass to the [Room].
 * @param connectOptions options to use when connecting. Will not reflect changes if already connected.
 * @param onConnected a listener to be called upon room connection.
 * @param onDisconnected a listener to be called upon room disconnection.
 * @param onError a listener to be called upon room error.
 * @param passedRoom if a [Room] is provided, it will be used. If null, a new Room will be created instead.
 */
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
    onError: ((Room, Exception?) -> Unit)? = null,
    passedRoom: Room? = null,
    content: @Composable (room: Room) -> Unit
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
        content = { content(room) },
    )
}

/**
 * Returns the [passedRoom] or the currently provided [RoomLocal].
 * @throws IllegalStateException if passedRoom is null and no RoomLocal is available (e.g. not inside a [RoomScope]).
 */
@Composable
@Throws(IllegalStateException::class)
fun requireRoom(passedRoom: Room? = null): Room {
    return passedRoom ?: RoomLocal.current
}

@SuppressLint("CompositionLocalNaming")
val RoomLocal =
    compositionLocalOf<Room> { throw IllegalStateException("No Room object available. This should only be used within a RoomScope.") }
