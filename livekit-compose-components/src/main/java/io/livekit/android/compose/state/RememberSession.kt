package io.livekit.android.compose.state

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.livekit.android.ConnectOptions
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.local.rememberLiveKitRoom
import io.livekit.android.room.ConnectionState
import io.livekit.android.room.Room
import io.livekit.android.room.participant.AudioTrackPublishOptions
import io.livekit.android.room.track.LocalAudioTrackOptions
import io.livekit.android.token.ConfigurableTokenSource
import io.livekit.android.token.FixedTokenSource
import io.livekit.android.token.TokenRequestOptions
import io.livekit.android.token.TokenSource
import io.livekit.android.util.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlin.time.Duration

data class SessionOptions(
    /**
     * The room to use. If null is passed, one will be created for you.
     */
    val room: Room? = null,

    /**
     * Amount of time to wait for an agent to join the room, before transitioning
     * to the failure state.
     */
    val agentConnectTimeout: Duration? = null,

    /**
     * The options to use when fetching the token, if it is fetching from
     * a [ConfigurableTokenSource].
     *
     * These options will be ignored for a [FixedTokenSource].
     */
    val tokenRequestOptions: TokenRequestOptions? = null
)

data class SessionConnectOptions(
    val tracks: SessionConnectTrackOptions = SessionConnectTrackOptions(),
    val roomConnectOptions: ConnectOptions = ConnectOptions()
)

/**
 * Track options for connection
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

interface Session {

    /** The [Room] object used for this session. */
    val room: Room

    /** The [ConnectionState] of the session. */
    val connectionState: ConnectionState

    /** Whether the session is connected or not. */
    val isConnected: Boolean

    /** Whether the session is reconnecting or not. */
    val isReconnecting: Boolean

    /**
     * A function that suspends until the session is connected.
     */
    suspend fun waitUntilConnected()

    /**
     * A function that suspends until the session is disconnected.
     */
    suspend fun waitUntilDisconnected()

    /**
     * Prepares the connection to speed up initial connection time.
     *
     * @see Room.prepareConnection
     */
    suspend fun prepareConnection()

    /**
     * Connect to the session.
     */
    @CheckResult
    suspend fun start(options: SessionConnectOptions = SessionConnectOptions()): Result<Unit>

    /**
     * Disconnect from the session.
     */
    fun end()
}

@Stable
internal class SessionImpl(
    override val room: Room,
    val connectionStateState: State<ConnectionState>,
    val waitUntilConnectedFn: suspend () -> Unit,
    val waitUntilDisconnectedFn: suspend () -> Unit,
    val prepareConnectionFn: suspend () -> Unit,
    val startFn: suspend (options: SessionConnectOptions) -> Result<Unit>,
    val endFn: () -> Unit
) : Session {
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

}

@Beta
@Composable
fun rememberSession(tokenSource: TokenSource, options: SessionOptions = SessionOptions()): Session {
    val room = rememberLiveKitRoom(passedRoom = options.room, connect = false)
    val connectionState = room::state.flow
        .map { state ->
            when (state) {
                Room.State.CONNECTING -> ConnectionState.CONNECTING
                Room.State.CONNECTED -> ConnectionState.CONNECTED
                Room.State.DISCONNECTED -> ConnectionState.DISCONNECTED
                Room.State.RECONNECTING -> ConnectionState.RECONNECTING
            }
        }
        .collectAsState(ConnectionState.DISCONNECTED)

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

    val tokenSourceFetch = remember(tokenSource, options.tokenRequestOptions) {
        suspend fetch@{
            return@fetch when (tokenSource) {
                is FixedTokenSource -> {
                    tokenSource.fetch()
                }

                is ConfigurableTokenSource -> {
                    tokenSource.fetch(options.tokenRequestOptions ?: TokenRequestOptions())
                }

                else -> {
                    throw IllegalArgumentException("tokenSource must either be a FixedTokenSource or ConfigurableTokenSource")
                }
            }
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
        )
    }

    return session
}

