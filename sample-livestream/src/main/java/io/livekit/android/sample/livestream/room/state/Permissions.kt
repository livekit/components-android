package io.livekit.android.sample.livestream.room.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun requirePermissions(enabled: Boolean) {

    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
    )

    DisposableEffect(enabled) {
        if (enabled && !permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
        onDispose { /* do nothing */ }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberEnableCamera(enabled: Boolean): Boolean {
    val permissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    return remember(enabled, permissionState) {
        derivedStateOf {
            enabled && permissionState.status.isGranted
        }
    }.value
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberEnableMic(enabled: Boolean): Boolean {
    val micPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )
    return remember(enabled, micPermissionState) {
        derivedStateOf {
            enabled && micPermissionState.status.isGranted
        }
    }.value
}