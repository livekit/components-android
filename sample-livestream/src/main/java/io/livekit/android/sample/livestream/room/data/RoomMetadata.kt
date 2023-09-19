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

package io.livekit.android.sample.livestream.room.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Serializable
data class RoomMetadata(
    @SerialName("creator_identity")
    val creatorIdentity: String,
    @SerialName("enable_chat")
    val enableChat: Boolean,
    @SerialName("allow_participant")
    val allowParticipation: Boolean
) {
    fun toJson(): String {
        return LKJson.encodeToString(this)
    }

    companion object {
        fun fromJson(str: String): RoomMetadata {
            return LKJson.decodeFromString(str)
        }
    }
}
