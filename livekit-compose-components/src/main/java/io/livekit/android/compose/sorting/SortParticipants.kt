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

package io.livekit.android.compose.sorting

import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.participant.Participant

/**
 * Default sort for participants, it'll order participants by:
 * 1. local participant
 * 2. dominant speaker (speaker with the loudest audio level)
 * 3. other speakers that are recently active
 * 4. participants with video on
 * 5. by joinedAt
 */
fun sortParticipants(participants: List<Participant>): List<Participant> {
    return participants.sortedWith { a, b ->
        if (a is LocalParticipant) {
            return@sortedWith -1
        }

        if (b is LocalParticipant) {
            return@sortedWith -1
        }

        if (a.isSpeaking && b.isSpeaking) {
            return@sortedWith compareAudioLevel(a, b)
        }

        if (a.lastSpokeAt != b.lastSpokeAt) {
            return@sortedWith compareLastSpokenAt(a, b)
        }

        val aHasVideo = a.videoTracks.any { it.first.subscribed }
        val bHasVideo = b.videoTracks.any { it.first.subscribed }
        if (aHasVideo != bHasVideo) {
            return@sortedWith compareValues(bHasVideo, aHasVideo)
        }

        compareJoinedAt(a, b)
    }
}

/**
 * A comparator to prefer loudest audio level.
 */
fun compareAudioLevel(a: Participant, b: Participant): Int {
    return compareValues(b.audioLevel, a.audioLevel)
}

/**
 * A comparator to prefer speaking participants first.
 */
fun compareIsSpeaking(a: Participant, b: Participant): Int {
    return compareValues(a.isSpeaking, b.isSpeaking)
}

/**
 * A comparator to prefer to newest speaker first
 */
fun compareLastSpokenAt(a: Participant, b: Participant): Int {
    return compareValues(b.lastSpokeAt, a.lastSpokeAt)
}

/**
 * A comparator to prefer to latest joining participant
 */
fun compareJoinedAt(a: Participant, b: Participant): Int {
    return compareValues(a.joinedAt, b.joinedAt)
}
