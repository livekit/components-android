package io.livekit.android.sample.livestream.ui.control

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTextButton(
    modifier: Modifier = Modifier,
    text: String = "",
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = PaddingValues(),
    onClick: () -> Unit,
) {

    CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
        Button(
            colors = colors,
            shape = RoundedCornerShape(5.dp),
            onClick = onClick,
            enabled = enabled,
            contentPadding = contentPadding,
            modifier = modifier
        ) {
            Text(text = text, modifier = Modifier.padding(0.dp))
        }
    }
}

@Preview
@Composable
fun SmallTextButtonPreview() {
    SmallTextButton(
        text = "Button",
        onClick = {},
        modifier = Modifier
            .defaultMinSize(60.dp, 24.dp)
    )
}