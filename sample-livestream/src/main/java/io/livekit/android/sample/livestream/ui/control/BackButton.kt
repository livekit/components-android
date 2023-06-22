package io.livekit.android.sample.livestream.ui.control

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun BackButton(onClick: () -> Unit) {
    val backPainter = rememberVectorPainter(image = Icons.Default.ArrowBack)
    IconButton(onClick = onClick) {
        Icon(backPainter, contentDescription = "Back")
    }
}