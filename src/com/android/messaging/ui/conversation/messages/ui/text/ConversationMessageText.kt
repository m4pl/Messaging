package com.android.messaging.ui.conversation.messages.ui.text

import android.os.SystemClock
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.android.messaging.ui.conversation.messages.model.text.ConversationTextLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val LINK_CLICK_SUPPRESSION_AFTER_LONG_PRESS_MILLIS = 500L

internal val LocalConversationMessageLinkColor: ProvidableCompositionLocal<Color?> =
    compositionLocalOf {
        null
    }

@Composable
internal fun ConversationMessageText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle,
    onExternalUriClick: (String) -> Unit,
    onMessageLongClick: () -> Unit,
) {
    val applicationContext = LocalContext.current.applicationContext
    val currentOnExternalUriClick by rememberUpdatedState(newValue = onExternalUriClick)
    val linkStyle = rememberConversationTextLinkStyle()
    val suppressLinkClickUntilUptimeMillis = remember { mutableLongStateOf(0L) }
    val textWithLinks by produceState(
        initialValue = AnnotatedString(text = text),
        applicationContext,
        text,
        linkStyle,
    ) {
        val links = withContext(Dispatchers.IO) {
            extractConversationTextLinks(
                context = applicationContext,
                text = text,
            )
        }

        value = buildConversationLinkedAnnotatedString(
            text = text,
            links = links,
            linkStyle = linkStyle,
            onExternalUriClick = { uri ->
                if (shouldSuppressConversationTextLinkClick(
                        suppressUntilUptimeMillis = suppressLinkClickUntilUptimeMillis.longValue,
                    )
                ) {
                    suppressLinkClickUntilUptimeMillis.longValue = 0L
                    return@buildConversationLinkedAnnotatedString
                }

                currentOnExternalUriClick(uri)
            },
        )
    }

    ConversationMessageTextContent(
        modifier = modifier,
        text = textWithLinks,
        style = style,
        onLinkLongPress = onMessageLongClick,
        suppressNextLinkClick = {
            suppressLinkClickUntilUptimeMillis.longValue =
                conversationTextLinkClickSuppressionDeadlineUptimeMillis()
        },
    )
}

@Composable
private fun ConversationMessageTextContent(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    style: TextStyle,
    onLinkLongPress: () -> Unit,
    suppressNextLinkClick: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val currentOnLinkLongPress by rememberUpdatedState(newValue = onLinkLongPress)
    val currentSuppressNextLinkClick by rememberUpdatedState(newValue = suppressNextLinkClick)
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val hasLinkAnnotations = text.hasLinkAnnotations(
        start = 0,
        end = text.length,
    )

    val textLongPressModifier = when {
        hasLinkAnnotations -> {
            Modifier.pointerInput(text, textLayoutResult) {
                detectConversationTextLinkLongPresses(
                    text = text,
                    textLayoutResult = textLayoutResult,
                    onLongPress = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnLinkLongPress()
                    },
                    suppressNextLinkClick = {
                        currentSuppressNextLinkClick()
                    },
                )
            }
        }

        else -> Modifier
    }

    Text(
        text = text,
        style = style,
        modifier = modifier.then(other = textLongPressModifier),
        onTextLayout = { result ->
            textLayoutResult = result
        },
    )
}

@Composable
private fun rememberConversationTextLinkStyle(): TextLinkStyles {
    val linkColor = LocalConversationMessageLinkColor.current
        ?: MaterialTheme.colorScheme.primary

    return remember(linkColor) {
        TextLinkStyles(
            style = SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline,
            ),
        )
    }
}

private fun conversationTextLinkClickSuppressionDeadlineUptimeMillis(): Long {
    return SystemClock.uptimeMillis() + LINK_CLICK_SUPPRESSION_AFTER_LONG_PRESS_MILLIS
}

private fun shouldSuppressConversationTextLinkClick(suppressUntilUptimeMillis: Long): Boolean {
    return SystemClock.uptimeMillis() <= suppressUntilUptimeMillis
}

private suspend fun PointerInputScope.detectConversationTextLinkLongPresses(
    text: AnnotatedString,
    textLayoutResult: TextLayoutResult?,
    onLongPress: () -> Unit,
    suppressNextLinkClick: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(
            requireUnconsumed = false,
            pass = PointerEventPass.Initial,
        )

        val hasConversationLinkAtPressPosition = textLayoutResult
            ?.hasConversationLinkAtPosition(text = text, position = down.position) == true

        if (!hasConversationLinkAtPressPosition) {
            return@awaitEachGesture
        }

        val isLongPressConfirmed = awaitConversationTextLongPressConfirmation()

        if (isLongPressConfirmed) {
            suppressNextLinkClick()
            onLongPress()
            consumeConversationTextGestureUntilUp()
            suppressNextLinkClick()
        }
    }
}

private fun TextLayoutResult.hasConversationLinkAtPosition(
    text: AnnotatedString,
    position: Offset,
): Boolean {
    val offset = getOffsetForPosition(position = position)
    val endOffset = (offset + 1).coerceAtMost(maximumValue = text.length)

    return when {
        offset >= endOffset -> false
        else -> {
            text.hasLinkAnnotations(
                start = offset,
                end = endOffset,
            )
        }
    }
}

private suspend fun AwaitPointerEventScope.consumeConversationTextGestureUntilUp() {
    var isPointerActive = true

    while (isPointerActive) {
        val event = awaitPointerEvent(pass = PointerEventPass.Initial)

        event.changes.forEach { change ->
            change.consume()
        }

        val allPointersUp = event.changes.all { change -> change.changedToUp() }

        if (allPointersUp) {
            isPointerActive = false
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitConversationTextLongPressConfirmation(): Boolean {
    try {
        withTimeout(timeMillis = viewConfiguration.longPressTimeoutMillis) {
            var isPointerActive = true

            while (isPointerActive) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                val hasPointerLeftBounds = event.changes.any { change ->
                    change.isOutOfBounds(
                        size = size,
                        extendedTouchPadding = extendedTouchPadding,
                    )
                }

                val allPointersUp = event.changes.all { change -> change.changedToUp() }

                if (allPointersUp) {
                    isPointerActive = false
                }

                if (hasPointerLeftBounds) {
                    isPointerActive = false
                }
            }
        }
    } catch (_: PointerEventTimeoutCancellationException) {
        return true
    }

    return false
}

private fun buildConversationLinkedAnnotatedString(
    text: String,
    links: List<ConversationTextLink>,
    linkStyle: TextLinkStyles,
    onExternalUriClick: (String) -> Unit,
): AnnotatedString {
    if (links.isEmpty()) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        var currentIndex = 0

        links.forEach { link ->
            if (link.start > currentIndex) {
                append(text.substring(currentIndex, link.start))
            }

            withLink(
                link = LinkAnnotation.Url(
                    url = link.url,
                    styles = linkStyle,
                    linkInteractionListener = { _ ->
                        onExternalUriClick(link.url)
                    },
                ),
            ) {
                append(text.substring(link.start, link.end))
            }

            currentIndex = link.end
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
