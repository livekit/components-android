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

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.livekit.android.ConnectOptions
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.local.rememberLiveKitRoom
import io.livekit.android.compose.types.AgentFailure
import io.livekit.android.room.ConnectionState
import io.livekit.android.room.Room
import io.livekit.android.room.participant.AudioTrackPublishOptions
import io.livekit.android.room.participant.isAgent
import io.livekit.android.room.track.LocalAudioTrackOptions
import io.livekit.android.token.ConfigurableTokenSource
import io.livekit.android.token.FixedTokenSource
import io.livekit.android.token.TokenRequestOptions
import io.livekit.android.token.TokenSource
import io.livekit.android.util.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Options for creating a [Session].
 * @see rememberSession
 */
data class SessionOptions(
    /**
     * The [Room] to use. If null is passed, one will be created for you.
     */
    val room: Room? = null,

    /**
     * Amount of time to wait for an agent to join the room, before transitioning the [Agent]
     * to the failure state.
     */
    // TODO: make this 10 seconds once room dispatch booting info is discoverable
    val agentConnectTimeout: Duration = 20.seconds,

    /**
     * The options to use when fetching the token, if it is fetching from
     * a [ConfigurableTokenSource].
     *
     * These options will be ignored for a [FixedTokenSource].
     */
    val tokenRequestOptions: TokenRequestOptions = TokenRequestOptions()
)

/**
 * Options for connecting to a session.
 * @see Session.start
 */
data class SessionConnectOptions(
    val tracks: SessionConnectTrackOptions = SessionConnectTrackOptions(),
    val roomConnectOptions: ConnectOptions = ConnectOptions()
)

/**
 * Track options for session connection.
 */
data class SessionConnectTrackOptions(
    /** Whether to enable microphone on connect. */
    val microphoneEnabled: Boolean = true,
    /** Whether to enable the preconnect audio buffer for faster perceived connection times */
    val usePreconnectBuffer: Boolean = true,
    /** @see LocalAudioTrackOptions */
    val microphoneCaptureOptions: LocalAudioTrackOptions = LocalAudioTrackOptions(),
    /** @see AudioTrackPublishOptions */
    val microphonePublishOptions: AudioTrackPublishOptions = AudioTrackPublishOptions(),
)

/**
 * Represents a managed connection to a [Room] which can contain an [Agent].
 */
@Beta
abstract class Session {

    /** The [Room] object used for this session. */
    abstract val room: Room

    /** The [ConnectionState] of the session. */
    abstract val connectionState: ConnectionState

    /** Whether the session is connected or not. */
    abstract val isConnected: Boolean

    /** Whether the session is reconnecting or not. */
    abstract val isReconnecting: Boolean

    /**
     * A function that suspends until the session is connected.
     */
    abstract suspend fun waitUntilConnected()

    /**
     * A function that suspends until the session is disconnected.
     */
    abstract suspend fun waitUntilDisconnected()

    /**
     * Prepares the connection to speed up initial connection time.
     *
     * @see Room.prepareConnection
     */
    abstract suspend fun prepareConnection()

    /**
     * Connect to the session.
     */
    @CheckResult
    abstract suspend fun start(options: SessionConnectOptions = SessionConnectOptions()): Result<Unit>

    /**
     * Disconnect from the session.
     */
    abstract fun end()

    internal abstract val agentFailure: AgentFailure?
}

@Beta
@Stable
internal class SessionImpl(
    override val room: Room,
    connectionStateState: State<ConnectionState>,
    agentFailureState: State<AgentFailure?>,
    private val waitUntilConnectedFn: suspend () -> Unit,
    private val waitUntilDisconnectedFn: suspend () -> Unit,
    private val prepareConnectionFn: suspend () -> Unit,
    private val startFn: suspend (options: SessionConnectOptions) -> Result<Unit>,
    private val endFn: () -> Unit,
) : Session() {
    override val connectionState by connectionStateState

    override val isConnected by derivedStateOf {
        when (connectionState) {
            ConnectionState.CONNECTED,
            ConnectionState.RECONNECTING,
            ConnectionState.RESUMING -> true

            ConnectionState.CONNECTING,
            ConnectionState.DISCONNECTED -> false
        }
    }

    override val isReconnecting by derivedStateOf {
        when (connectionState) {
            ConnectionState.RECONNECTING,
            ConnectionState.RESUMING -> true

            ConnectionState.CONNECTING,
            ConnectionState.CONNECTED,
            ConnectionState.DISCONNECTED -> false
        }
    }

    override suspend fun waitUntilConnected() {
        waitUntilConnectedFn()
    }

    override suspend fun waitUntilDisconnected() {
        waitUntilDisconnectedFn()
    }

    override suspend fun prepareConnection() {
        prepareConnectionFn()
    }

    override suspend fun start(options: SessionConnectOptions): Result<Unit> {
        return startFn(options)
    }

    override fun end() {
        endFn()
    }

    override val agentFailure: AgentFailure? by agentFailureState
}

/**
 * Creates and manages a [Session] object.
 *
 * @param tokenSource The [TokenSource] that should be used to connect to the [Room]
 * @param options Options to be used for this session.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Beta
@Composable
fun rememberSession(tokenSource: TokenSource, options: SessionOptions = SessionOptions()): Session {
    val room = rememberLiveKitRoom(passedRoom = options.room, connect = false)
    val connectionState = produceState(ConnectionState.DISCONNECTED, room) {
        room::state.flow
            .map { state ->
                when (state) {
                    Room.State.CONNECTING -> ConnectionState.CONNECTING
                    Room.State.CONNECTED -> ConnectionState.CONNECTED
                    Room.State.DISCONNECTED -> ConnectionState.DISCONNECTED
                    Room.State.RECONNECTING -> ConnectionState.RECONNECTING
                }
            }
            .collect {
                value = it
            }
    }

    val waitUntilConnected = remember(room) {
        suspend {
            room::state.flow
                .takeWhile { it != Room.State.CONNECTED }
                .collect()
        }
    }

    val waitUntilDisconnected = remember(room) {
        suspend {
            room::state.flow
                .takeWhile { it != Room.State.DISCONNECTED }
                .collect()
        }
    }

    val tokenSource by rememberUpdatedState(tokenSource)
    val tokenRequestOptions by rememberUpdatedState(options.tokenRequestOptions)
    val tokenSourceFetch = remember {
        suspend fetch@{
            val source = tokenSource
            return@fetch when (source) {
                is FixedTokenSource -> {
                    source.fetch()
                }

                is ConfigurableTokenSource -> {
                    source.fetch(tokenRequestOptions)
                }

                else -> {
                    throw IllegalArgumentException("tokenSource must either be a FixedTokenSource or ConfigurableTokenSource")
                }
            }
        }
    }

    val agentTimeoutDuration by rememberUpdatedState(options.agentConnectTimeout)
    val isSessionDisconnected by rememberUpdatedState(connectionState.value == ConnectionState.DISCONNECTED)
    val agentFailureState = produceState<AgentFailure?>(null, isSessionDisconnected) {
        value = null
        if (isSessionDisconnected) {
            return@produceState
        }

        val participant = withContext(Dispatchers.IO) {
            withTimeoutOrNull(agentTimeoutDuration) {
                // Take until we get an agent participant.
                room::remoteParticipants.flow
                    .map { it -> it.values }
                    .map { remoteParticipants ->
                        remoteParticipants
                            .filter { p -> p.agentAttributes.lkPublishOnBehalf == null }
                            .firstOrNull { p -> p.isAgent }
                    }
                    .mapNotNull { it }
                    .first()
            }
        }

        ensureActive()
        value = if (participant != null) {
            null
        } else {
            AgentFailure.TIMEOUT
        }
    }
    val start = remember(room, waitUntilDisconnected, waitUntilConnected, tokenSourceFetch) {
        val startImpl: suspend (SessionConnectOptions) -> Result<Unit> = { sessionConnectOptions ->

            waitUntilDisconnected()

            @CheckResult
            suspend fun connect(): Result<Unit> {
                val fetchResult = tokenSourceFetch()
                if (fetchResult.isFailure) {
                    return Result.failure(fetchResult.exceptionOrNull() ?: NullPointerException())
                }

                val credentials = fetchResult.getOrThrow()
                val connectOptions = sessionConnectOptions.roomConnectOptions
                    .copy(audio = sessionConnectOptions.tracks.microphoneEnabled)

                try {
                    room.connect(
                        url = credentials.serverUrl,
                        token = credentials.participantToken,
                        options = connectOptions,
                    )
                } catch (e: Exception) {
                    return Result.failure(e)
                }

                return Result.success(Unit)
            }

            val result = connect()

            if (result.isSuccess) {
                waitUntilConnected()
            }

            result
        }

        startImpl
    }

    val end = remember(room) {
        {
            room.disconnect()
        }
    }

    val prepareConnection = remember(room, tokenSourceFetch) {
        suspend {
            val fetchResult = tokenSourceFetch()
            if (fetchResult.isSuccess) {
                val credentials = fetchResult.getOrThrow()
                room.prepareConnection(credentials.serverUrl, credentials.participantToken)
            }
        }
    }

    // Only prepare connection once ever.
    LaunchedEffect(Unit) {
        prepareConnection()
    }

    val session = remember(room) {
        SessionImpl(
            room = room,
            connectionStateState = connectionState,
            waitUntilConnectedFn = waitUntilConnected,
            waitUntilDisconnectedFn = waitUntilDisconnected,
            prepareConnectionFn = prepareConnection,
            startFn = start,
            endFn = end,
            agentFailureState = agentFailureState
        )
    }

    return session
}
