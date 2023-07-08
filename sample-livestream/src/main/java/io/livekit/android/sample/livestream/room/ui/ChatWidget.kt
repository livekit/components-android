package io.livekit.android.sample.livestream.room.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.livekit.android.sample.livestream.ui.control.LKTextField
import io.livekit.android.sample.livestream.ui.control.Spacer
import io.livekit.android.sample.livestream.room.screen.nameToColor
import io.livekit.android.sample.livestream.ui.theme.AppTheme
import io.livekit.android.sample.livestream.ui.theme.Blue500
import io.livekit.android.sample.livestream.ui.theme.Dimens
import io.livekit.android.sample.livestream.ui.theme.LightLine

data class ChatWidgetMessage(
    val name: String,
    val message: String,
)

/**
 * A composable for displaying all chats that come through the livestream.
 */
@Composable
fun ChatWidget(
    // Ordered from oldest to newest.
    messages: List<ChatWidgetMessage>,
    onChatSend: (String) -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(modifier = modifier) {
        val (chatBar, chatLogs) = createRefs()

        LazyColumn(
            reverseLayout = true,
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
                .constrainAs(chatLogs) {
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(chatBar.top)
                }
        ) {
            items(
                items = messages.asReversed(),
                key = { it.hashCode() }
            ) { message ->
                Spacer(20.dp)
                Row(verticalAlignment = Alignment.Top) {
                    Canvas(modifier = Modifier.size(32.dp), onDraw = {
                        drawCircle(color = nameToColor(message.name))
                    })
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

        ChatBar(
            onChatSend = onChatSend,
            onOptionsClick = onOptionsClick,
            modifier = Modifier.constrainAs(chatBar) {
                width = Dimension.fillToConstraints
                height = Dimension.wrapContent
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBar(
    onChatSend: (String) -> Unit,
    onOptionsClick: () -> Unit,
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
            placeholder = {
                Text("Type your message...")
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
                "I struggle with procrastination. Any tips to overcome it?"
            ),
            ChatWidgetMessage(
                "FitnessGuru21",
                "Thanks for joining, WellnessEnthusiast22! Today we'll be discussing tips for staying motivated and productive. Feel free to ask questions too!"
            ),
            ChatWidgetMessage(
                "WellnessEnthusiast22",
                "Hey there! Just joined the live. What's the topic for today?"
            ),
        )
    }
    AppTheme {
        ChatWidget(
            messages = messages,
            onChatSend = {
                messages.add(
                    index = 0,
                    ChatWidgetMessage(
                        "You",
                        it
                    )
                )
            },
            onOptionsClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}