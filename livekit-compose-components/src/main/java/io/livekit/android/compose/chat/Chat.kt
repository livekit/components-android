/*
 * Copyright 2023-2025 LiveKit, Inc.
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

package io.livekit.android.compose.chat

import android.annotation.SuppressLint
import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.vdurmont.semver4j.Semver
import io.livekit.android.compose.flow.DataHandler
import io.livekit.android.compose.flow.DataSendOptions
import io.livekit.android.compose.flow.DataTopic
import io.livekit.android.compose.flow.LegacyDataTopic
import io.livekit.android.compose.flow.rememberDataMessageHandler
import io.livekit.android.compose.local.RoomLocal
import io.livekit.android.compose.types.ReceivedChatMessage
import io.livekit.android.room.Room
import io.livekit.android.room.ServerInfo
import io.livekit.android.room.datastream.StreamTextOptions
import io.livekit.android.room.datastream.incoming.TextStreamReceiver
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.DataPublishReliability
import io.livekit.android.util.LKLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.UUID

/**
 * Chat state for sending messages through LiveKit.
 */
class Chat(
    private val localParticipant: LocalParticipant,
    private val dataHandler: DataHandler,
    private val serverSupportsDataStreams: () -> Boolean,
) {
    private val stateLock = Mutex()

    private val sendLock = Mutex()

    /**
     * Send a message through LiveKit.
     *
     * @param message
     */
    @CheckResult
    suspend fun send(
        message: String,
        streamTextOptions: StreamTextOptions = StreamTextOptions(topic = DataTopic.CHAT.value)
    ): Result<ReceivedChatMessage> {
        val streamTextOptions = if (streamTextOptions.topic.isEmpty()) {
            streamTextOptions.copy(topic = DataTopic.CHAT.value)
        } else {
            streamTextOptions
        }
        var retMessage: ReceivedChatMessage?

        sendLock.withLock {
            _isSending.value = true

            val result = localParticipant.sendText(message, streamTextOptions)
            val timestamp = Date().time

            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception())
            }

            val streamInfo = result.getOrThrow()
            val sentMessage = ReceivedChatMessage(
                id = streamInfo.id,
                message = message,
                timestamp = timestamp,
                fromParticipant = localParticipant
            )
            retMessage = sentMessage

            // Legacy chat sending
            // participant is filled in server side.
            val chatMessage = LegacyChatMessage(
                id = UUID.randomUUID().toString(),
                timestamp = timestamp,
                message = message,
                ignoreLegacy = serverSupportsDataStreams(),
            )

            val encodedMessage = Json.encodeToString(chatMessage).toByteArray(Charsets.UTF_8)
            dataHandler.sendMessage(
                payload = encodedMessage,
                options = DataSendOptions(reliability = DataPublishReliability.RELIABLE)
            )

            // add the messages to the local log.
            addMessage(sentMessage)
            _isSending.value = false
        }

        return retMessage?.let {
            Result.success(it)
        } ?: Result.failure(NullPointerException())
    }

    /**
     * Add a message directly to the messages log.
     */
    internal suspend fun addMessage(chatMessage: ReceivedChatMessage) {
        stateLock.withLock {
            // If we have same id from same participant, replace it.
            val messageList = messages.value
            val existingIndex =
                messageList.indexOfFirst { it.id == chatMessage.id && it.fromParticipant?.identity == chatMessage.fromParticipant?.identity }

            if (existingIndex >= 0) {
                val mutatedList = messageList.toMutableList()
                val original = messageList[existingIndex]
                mutatedList[existingIndex] = chatMessage
                    .copy(timestamp = original.timestamp, editTimestamp = chatMessage.timestamp)
                messages.value = mutatedList
            } else {
                messages.value = messages.value.plus(chatMessage)
            }

            mutableMessagesFlow.tryEmit(chatMessage)
        }
    }

    private val _isSending = mutableStateOf(false)

    /**
     * Indicates if currently sending a chat message.
     */
    val isSending: State<Boolean> = _isSending

    /**
     * The log of all messages sent and received.
     */
    val messages = mutableStateOf(emptyList<ReceivedChatMessage>())

    private val mutableMessagesFlow = MutableSharedFlow<ReceivedChatMessage>(extraBufferCapacity = 1000)

    /**
     * A hot flow emitting a [ReceivedChatMessage] for each individual message sent and received.
     */
    val messagesFlow = mutableMessagesFlow as Flow<ReceivedChatMessage>
}

/**
 * A chat message.
 */
@SuppressLint("UnsafeOptInUsageError")
@Deprecated(message = "Deprecated in favor of ReceivedChatMessage")
@Serializable
data class LegacyChatMessage(
    val id: String? = null,
    /** Millis since UNIX epoch */
    val timestamp: Long,
    /** The message */
    val message: String,
    /**
     * The participant who sent to message, if available.
     *
     * Messages sent by the server will have a null participant.
     */
    @Transient
    val participant: Participant? = null,

    internal val ignoreLegacy: Boolean? = false,
)

/**
 * Creates a [Chat] that is remembered across compositions.
 *
 * Changing the [room] value will result in a new [Chat] object being created.
 */
@Composable
fun rememberChat(room: Room = RoomLocal.current): Chat {
    val serverSupportsDataStreams = remember(room) {
        // lambda function
        canSupport@{
            val version = room.serverInfo?.version
            return@canSupport room.serverInfo?.edition == ServerInfo.Edition.CLOUD ||
                (version != null && version > Semver("1.8.2"))
        }
    }
    val dataHandler = rememberDataMessageHandler(room = room, topic = LegacyDataTopic.CHAT) // Legacy data handler

    val chatState = remember(dataHandler) {
        Chat(
            localParticipant = room.localParticipant,
            dataHandler = dataHandler,
            serverSupportsDataStreams = serverSupportsDataStreams,
        )
    }

    // Data stream for chat.
    val coroutineScope = rememberCoroutineScope()
    val chatDataStream = remember(room) { setupChatDataStream(room, coroutineScope) }
    DisposableEffect(room) {
        onDispose {
            cleanupChatDataStream(room)
        }
    }

    LaunchedEffect(chatDataStream, chatState) {
        chatDataStream.collect { message ->
            chatState.addMessage(message)
        }
    }

    // Legacy chat receiving
    LaunchedEffect(dataHandler, chatState) {
        dataHandler.messageFlow
            .collect { dataMessage ->
                val payloadString = dataMessage.payload.decodeToString()
                try {
                    val legacyChatMessage = json.decodeFromString<LegacyChatMessage>(payloadString)
                    if (legacyChatMessage.ignoreLegacy == false) {
                        val chatMessage = ReceivedChatMessage(
                            id = legacyChatMessage.id ?: UUID.randomUUID().toString(),
                            message = legacyChatMessage.message,
                            timestamp = legacyChatMessage.timestamp,
                            fromParticipant = legacyChatMessage.participant,
                        )
                        chatState.addMessage(chatMessage)
                    }
                } catch (e: Exception) {
                    LKLog.e(e) { "malformed chat message: $payloadString" }
                }
            }
    }

    return chatState
}

private fun setupChatDataStream(
    room: Room,
    coroutineScope: CoroutineScope,
    topic: String = DataTopic.CHAT.value
): SharedFlow<ReceivedChatMessage> {
    // The output flow
    val outputFlow = MutableSharedFlow<ReceivedChatMessage>()

    room.registerTextStreamHandler(topic) { reader: TextStreamReceiver, fromIdentity: Participant.Identity ->
        val participant = room.getParticipantByIdentity(fromIdentity)
        coroutineScope.launch {
            // Gather up the text
            reader.flow
                .scan("") { accumulator, value -> accumulator + value }
                .drop(1)
                .map { text ->
                    ReceivedChatMessage(
                        id = reader.info.id,
                        message = text,
                        timestamp = reader.info.timestampMs,
                        fromParticipant = participant,
                    )
                }
                .collect { message ->
                    outputFlow.emit(message)
                }
        }
    }

    return outputFlow
}

private fun cleanupChatDataStream(room: Room, topic: String = DataTopic.CHAT.value) {
    room.unregisterTextStreamHandler(topic)
}

private val json = Json {
    ignoreUnknownKeys = true
}
