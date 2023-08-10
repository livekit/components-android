package io.livekit.android.sample.livestream.room.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.livekit.android.sample.livestream.ui.control.LKTextField
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.ui.theme.AppTheme
import io.livekit.android.sample.livestream.ui.theme.Blue500
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.ui.theme.LightLine
import java.util.Date

data class ChatWidgetMessage(
    val name: String,
    val message: String,
    val avatarUrl: String,
    val timestamp: Long,
)

/**
 * A composable for displaying all chats that come through the livestream.
 */
@Composable
fun ChatLog(
    // Ordered from oldest to newest.
    messages: List<ChatWidgetMessage>,
    modifier: Modifier = Modifier,
) {

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        listState.scrollToItem(0)
    }
    LazyColumn(
        reverseLayout = true,
        state = listState,
        modifier = Modifier
            .padding(horizontal = Dimens.spacer)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                val colors = arrayOf(
                    0.0f to Color.Transparent,
                    0.3f to Color.Black,
                    1.0f to Color.Black,
                )
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = colors
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
            .then(modifier)
    ) {

        items(
            items = messages.asReversed(),
            key = { it.hashCode() }
        ) { message ->
            Spacer(20.dp)
            Row(verticalAlignment = Alignment.Top) {
                AvatarIcon(
                    imageUrl = message.avatarUrl,
                    name = message.name,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(12.dp)
                Column {
                    Text(
                        text = message.name,
                        fontWeight = FontWeight.W700,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = message.message,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
        item {
            Spacer(50.dp)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBar(
    onChatSend: (String) -> Unit,
    onOptionsClick: () -> Unit,
    chatEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
            .drawBehind {
                val strokeWidth = 1 * density
                drawLine(
                    LightLine,
                    Offset(0f, 0f),
                    Offset(size.width, 0f),
                    strokeWidth
                )
            }
            .background(MaterialTheme.colorScheme.surface)
            .padding(Dimens.spacer)
    ) {
        val (sendButton, optionsButton, messageInput) = createRefs()

        var message by rememberSaveable {
            mutableStateOf("")
        }
        LKTextField(
            value = message,
            onValueChange = { message = it },
            shape = RoundedCornerShape(5.dp),
            colors = TextFieldDefaults.textFieldColors(
                textColor = Color.White,
                disabledTextColor = Color.Transparent,
                containerColor = Color(0xFF222222),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            enabled = chatEnabled,
            placeholder = {
                if (chatEnabled) {
                    Text("Type your message...")
                } else {
                    Text("Chat disabled", style = TextStyle(fontStyle = FontStyle.Italic))
                }
            },
            modifier = Modifier
                .constrainAs(messageInput) {
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                    start.linkTo(parent.start)
                    end.linkTo(sendButton.start, Dimens.spacer)
                    bottom.linkTo(parent.bottom)
                }
        )

        val sendButtonColors = ButtonDefaults.buttonColors(
            containerColor = Blue500,
            contentColor = Color.White
        )

        Button(
            colors = sendButtonColors,
            shape = RoundedCornerShape(5.dp),
            onClick = {
                onChatSend(message)
                message = ""
            },
            enabled = message.isNotEmpty(),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(0.dp)
                .constrainAs(sendButton) {
                    width = Dimension.wrapContent
                    height = Dimension.value(42.dp)
                    end.linkTo(optionsButton.start)
                    top.linkTo(messageInput.top)
                }
        ) {
            Text(text = "Send", modifier = Modifier.padding(0.dp))
        }


        IconButton(
            onClick = {
                onOptionsClick()
            },
            modifier = Modifier
                .constrainAs(optionsButton) {
                    width = Dimension.wrapContent
                    height = Dimension.value(42.dp)
                    end.linkTo(parent.end)
                    top.linkTo(messageInput.top)
                }
        ) {
            Icon(
                painter = rememberVectorPainter(image = Icons.Default.MoreVert),
                contentDescription = "More"
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun ChatWidgetPreview() {
    val messages = remember {
        mutableStateListOf(
            ChatWidgetMessage(
                "HealthyLifestyle101",
                "I struggle with procrastination. Any tips to overcome it?",
                "",
                Date().time
            ),
            ChatWidgetMessage(
                "FitnessGuru21",
                "Thanks for joining, WellnessEnthusiast22! Today we'll be discussing tips for staying motivated and productive. Feel free to ask questions too!",
                "",
                Date().time
            ),
            ChatWidgetMessage(
                "WellnessEnthusiast22",
                "Hey there! Just joined the live. What's the topic for today?",
                "",
                Date().time
            ),
        )
    }
    AppTheme {
        Column {
            ChatLog(
                messages = messages,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            ChatBar(
                onChatSend = {
                    messages.add(
                        index = 0,
                        ChatWidgetMessage(
                            "You",
                            it,
                            "",
                            Date().time
                        )
                    )
                },
                onOptionsClick = {},
                chatEnabled = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}