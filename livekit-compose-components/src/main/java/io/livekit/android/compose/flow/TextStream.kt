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

package io.livekit.android.compose.flow

import io.livekit.android.room.Room
import io.livekit.android.room.datastream.StreamInfo
import io.livekit.android.room.datastream.incoming.TextStreamReceiver
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.types.TranscriptionAttributes
import io.livekit.android.room.types.fromMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * A representation of the aggregated data from a text stream.
 */
data class TextStreamData(
    /**
     * The aggregated text.
     */
    val text: String,
    /**
     * The participant
     */
    val participantIdentity: Participant.Identity,
    /**
     * The info for a stream.
     */
    val streamInfo: StreamInfo
)

/**
 * Registers a text stream handler for [topic], and returns a flow of lists containing all the text stream data
 * for each received stream.
 */
internal fun setupTextStream(room: Room, topic: String, coroutineScope: CoroutineScope): Flow<List<TextStreamData>> {
    // The output flow
    val textStreamFlow = MutableStateFlow<List<TextStreamData>>(emptyList())

    val textStreams = Collections.synchronizedList(mutableListOf<TextStreamData>())
    room.registerTextStreamHandler(topic) { reader: TextStreamReceiver, fromIdentity: Participant.Identity ->
        val transcriptionAttributes = TranscriptionAttributes.fromMap(reader.info.attributes)
        val isTranscription = transcriptionAttributes.lkSegmentID != null

        var index = -1

        coroutineScope.launch(Dispatchers.IO) {
            // Gather up the text
            reader.flow
                .scan("") { accumulator, value -> accumulator + value }
                .drop(1)
                .collect { nextText ->
                    synchronized(textStreams) {
                        if (index == -1) {
                            index = textStreams.indexOfFirst { stream ->
                                val streamTranscriptionAttributes = if (isTranscription) {
                                    TranscriptionAttributes.fromMap(stream.streamInfo.attributes)
                                } else {
                                    null
                                }
                                return@indexOfFirst stream.streamInfo.id == reader.info.id ||
                                    (isTranscription && streamTranscriptionAttributes?.lkSegmentID == transcriptionAttributes.lkSegmentID)
                            }
                        }

                        if (index == -1) {
                            // New stream, add it
                            textStreams.add(
                                TextStreamData(
                                    text = nextText,
                                    participantIdentity = fromIdentity,
                                    streamInfo = reader.info,
                                )
                            )
                        } else {
                            val newData = textStreams[index].copy(text = nextText)
                            textStreams[index] = newData
                        }

                        // Always make copy of list to prevent concurrent modification errors.
                        textStreamFlow.tryEmit(textStreams.toList())
                    }
                }
        }
    }

    return textStreamFlow
}
