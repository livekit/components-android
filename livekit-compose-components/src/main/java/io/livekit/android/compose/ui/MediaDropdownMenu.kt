/*
 * Copyright 2023-2024 LiveKit, Inc.
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

package io.livekit.android.compose.ui

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import livekit.org.webrtc.Camera1Enumerator
import livekit.org.webrtc.Camera2Enumerator

data class DeviceState(val selectedDeviceId: String, val deviceIds: List<String>)

/**
 * Creates a [DeviceState] that is remembered across recompositions.
 */
@Composable
fun rememberCameraState(): MutableState<DeviceState> {
    val cameraState = remember {
        mutableStateOf(
            DeviceState(
                "",
                emptyList()
            )
        )
    }

    val context = LocalContext.current
    LaunchedEffect(context) {
        val enumerator = if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator(true)
        }
        val deviceIds = enumerator.deviceNames
        cameraState.value = DeviceState(
            deviceIds.first(),
            deviceIds.toList(),
        )
    }

    return cameraState
}

/**
 * A dropdown menu that enables selection of the available cameras.
 *
 * When a camera is selected, [cameraState]'s value will be updated
 * with the selected device.
 *
 * Example:
 * ```
 * val deviceState = rememberCameraState()
 * MediaDeviceMenu(cameraState = deviceState)
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDeviceMenu(
    cameraState: MutableState<DeviceState>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier,
    ) {
        TextField(
            // The `menuAnchor` modifier must be passed to the text field for correctness.
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = cameraState.value.selectedDeviceId,
            onValueChange = {},
            label = { Text("Camera") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            cameraState.value.deviceIds.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        cameraState.value = cameraState.value.copy(selectedDeviceId = selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
