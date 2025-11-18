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

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.chat.rememberChat
import io.livekit.android.compose.local.requireSession
import io.livekit.android.compose.state.transcriptions.rememberTranscriptions
import io.livekit.android.compose.types.ReceivedAgentTranscriptionMessage
import io.livekit.android.compose.types.ReceivedChatMessage
import io.livekit.android.compose.types.ReceivedMessage
import io.livekit.android.compose.types.ReceivedUserTranscriptionMessage
import io.livekit.android.room.datastream.StreamTextOptions
import kotlinx.coroutines.launch

/**
 * Handles all the messages that are sent and received within a [Session].
 */
@Beta
abstract class SessionMessages {
    /**
     * The log of all messages sent and received.
     */
    abstract val messages: List<ReceivedMessage>

    /**
     * Indicates if currently sending a chat message.
     */
    abstract val isSending: Boolean

    /**
     * Send a message through LiveKit.
     *
     * @param message
     */
    abstract suspend fun send(message: String, options: StreamTextOptions = StreamTextOptions()): Result<ReceivedChatMessage>
}

@Beta
internal class SessionMessagesImpl(
    messagesState: State<List<ReceivedMessage>>,
    isSendingState: State<Boolean>,
    val sendImpl: suspend (message: String, options: StreamTextOptions) -> Result<ReceivedChatMessage>
) : SessionMessages() {
    override val messages by messagesState
    override val isSending by isSendingState

    override suspend fun send(
        message: String,
        options: StreamTextOptions,
    ): Result<ReceivedChatMessage> {
        return sendImpl(message, options)
    }
}

@Beta
@Composable
fun rememberSessionMessages(session: Session? = null): SessionMessages {
    val session = requireSession(session)
    val room = session.room
    val agent = rememberAgent(session)

    val transcriptions by rememberTranscriptions(room)
    val chat = rememberChat(room = room)
    val isSendingState = chat.isSending
    val transcriptionMessages by remember(room) {
        derivedStateOf {
            transcriptions.map { transcription ->
                when (transcription.participantIdentity) {
                    room.localParticipant.identity -> {
                        // User transcription
                        ReceivedUserTranscriptionMessage(
                            id = transcription.streamInfo.id,
                            message = transcription.text,
                            timestamp = transcription.streamInfo.timestampMs,
                            fromParticipant = room.localParticipant,
                        )
                    }

                    agent.agentParticipant?.identity,
                    agent.workerParticipant?.identity -> {
                        ReceivedAgentTranscriptionMessage(
                            id = transcription.streamInfo.id,
                            message = transcription.text,
                            timestamp = transcription.streamInfo.timestampMs,
                            fromParticipant = if (agent.agentParticipant?.identity == transcription.participantIdentity) {
                                agent.agentParticipant!!
                            } else {
                                agent.workerParticipant!!
                            },
                        )
                    }

                    else -> {
                        // FIXME: what should happen if an associated participant is not found?
                        //
                        // For now, just assume it is an agent transcription, since maybe it is from an agent
                        // which disconnected from the room or something like that.
                        ReceivedAgentTranscriptionMessage(
                            id = transcription.streamInfo.id,
                            message = transcription.text,
                            timestamp = transcription.streamInfo.timestampMs,
                            fromParticipant = room.remoteParticipants.values.firstOrNull { p -> p.identity == transcription.participantIdentity }
                        )
                    }
                }
            }
        }
    }

    val receivedMessagesTimeMap = remember {
        mutableStateMapOf<String, Long>()
    }
    val receivedMessages = remember {
        derivedStateOf {
            (transcriptionMessages.plus(elements = chat.messages.value))
                .sortedBy { receivedMessagesTimeMap[it.id] }
        }
    }

    LaunchedEffect(Unit) {
        launch {
            snapshotFlow { transcriptionMessages }
                .collect { messages ->
                    for (message in messages) {
                        val original = receivedMessagesTimeMap[message.id]
                        if (original == null) {
                            receivedMessagesTimeMap[message.id] = SystemClock.elapsedRealtime()
                        }
                    }
                }
        }
        launch {
            chat.messagesFlow
                .collect { message -> receivedMessagesTimeMap[message.id] = SystemClock.elapsedRealtime() }
        }
    }

    val sessionMessages = remember(chat) {
        SessionMessagesImpl(
            messagesState = receivedMessages,
            isSendingState = isSendingState,
            { message, options ->
                chat.send(message, options)
            }
        )
    }

    return sessionMessages
}
