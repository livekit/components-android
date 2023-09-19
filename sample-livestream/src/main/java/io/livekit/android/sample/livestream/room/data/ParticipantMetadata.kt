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
data class ParticipantMetadata(
    // true if participant requested to join stage
    @SerialName("hand_raised")
    val handRaised: Boolean = false,
    /**
     * true if participant has been invited to stage by the host
     */
    @SerialName("invited_to_stage")
    val invitedToStage: Boolean = false,

    @SerialName("avatar_image")
    val avatarImageUrl: String = ""
) {

    /**
     * true if the participant is both handRaised and invited to stage.
     */
    val isOnStage
        get() = handRaised && invitedToStage

    fun toJson(): String {
        return LKJson.encodeToString(this)
    }

    fun avatarImageUrlWithFallback(identity: String) =
        if (avatarImageUrl.isNotBlank()) {
            avatarImageUrl
        } else {
            "https://api.multiavatar.com/$identity.png"
        }

    companion object {
        fun fromJson(str: String): ParticipantMetadata {
            return LKJson.decodeFromString(str)
        }
    }
}
