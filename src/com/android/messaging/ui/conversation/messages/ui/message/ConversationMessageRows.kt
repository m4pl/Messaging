package com.android.messaging.ui.conversation.messages.ui.message

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.android.messaging.ui.conversation.messages.model.message.ConversationMessageUiModel

@Composable
internal fun ConversationMessageBubbleRow(
    message: ConversationMessageUiModel,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    layout: ConversationMessageLayout,
    maxBubbleWidth: Dp,
    onAttachmentClick: (contentType: String, contentUri: String) -> Unit,
    onExternalUriClick: (String) -> Unit,
    onMessageClick: () -> Unit,
    onMessageLongClick: () -> Unit,
    onMessageResendClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .conversationMessageSelectionModeRowModifier(
                isSelected = isSelected,
                isSelectionMode = isSelectionMode,
                onMessageClick = onMessageClick,
                onMessageLongClick = onMessageLongClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConversationMessageSelectionIndicator(
            visible = isSelectionMode,
            isSelected = isSelected,
            expandFrom = Alignment.Start,
            shrinkTowards = Alignment.Start,
        )

        Row(
            modifier = Modifier.weight(weight = 1f),
            horizontalArrangement = conversationMessageRowHorizontalArrangement(
                message = message,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ConversationMessageBubble(
                modifier = Modifier.conversationMessageBubbleInteractionModifier(
                    message = message,
                    isSelectionMode = isSelectionMode,
                    layout = layout,
                    onMessageLongClick = onMessageLongClick,
                    onMessageResendClick = onMessageResendClick,
                ),
                message = message,
                isSelected = isSelected,
                isSelectionMode = isSelectionMode,
                layout = layout,
                maxBubbleWidth = maxBubbleWidth,
                onAttachmentClick = { contentType, contentUri ->
                    when {
                        isSelectionMode -> onMessageClick()
                        message.canResendMessage -> onMessageResendClick()
                        else -> onAttachmentClick(contentType, contentUri)
                    }
                },
                onExternalUriClick = { uri ->
                    when {
                        isSelectionMode -> onMessageClick()
                        message.canResendMessage -> onMessageResendClick()
                        else -> onExternalUriClick(uri)
                    }
                },
                onMessageLongClick = onMessageLongClick,
            )
        }
    }
}

private fun conversationMessageRowHorizontalArrangement(
    message: ConversationMessageUiModel,
): Arrangement.Horizontal {
    return when {
        message.isIncoming -> Arrangement.Start
        else -> Arrangement.End
    }
}

@Composable
private fun Modifier.conversationMessageSelectionModeRowModifier(
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onMessageClick: () -> Unit,
    onMessageLongClick: () -> Unit,
): Modifier {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    return when {
        !isSelectionMode -> this

        else -> {
            this
                .semantics {
                    role = Role.Checkbox
                    selected = isSelected
                }
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = true,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onMessageClick()
                    },
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMessageLongClick()
                    },
                )
        }
    }
}

@Composable
private fun Modifier.conversationMessageBubbleInteractionModifier(
    message: ConversationMessageUiModel,
    isSelectionMode: Boolean,
    layout: ConversationMessageLayout,
    onMessageLongClick: () -> Unit,
    onMessageResendClick: () -> Unit,
): Modifier {
    val hapticFeedback = LocalHapticFeedback.current
    val bubbleModifier = this
        .clip(shape = layout.bubbleShape)

    return when {
        isSelectionMode -> bubbleModifier

        else -> {
            bubbleModifier.combinedClickable(
                enabled = true,
                onClick = {
                    if (message.canResendMessage) {
                        onMessageResendClick()
                    }
                },
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onMessageLongClick()
                },
            )
        }
    }
}

@Composable
internal fun ConversationMessageMetadataRow(
    message: ConversationMessageUiModel,
    isSelectionMode: Boolean,
    layout: ConversationMessageLayout,
    maxBubbleWidth: Dp,
    simDisplayName: String?,
    onSimSelectorClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ConversationMessageSelectionIndicatorOffset(
            visible = isSelectionMode,
            expandFrom = Alignment.Start,
            shrinkTowards = Alignment.Start,
        )

        Row(
            modifier = Modifier.weight(weight = 1f),
            horizontalArrangement = conversationMessageRowHorizontalArrangement(
                message = message,
            ),
        ) {
            Column(
                modifier = Modifier.widthIn(max = maxBubbleWidth),
                horizontalAlignment = when {
                    message.isIncoming -> Alignment.Start
                    else -> Alignment.End
                },
            ) {
                ConversationMessageMetadata(
                    message = message,
                    metadataText = layout.metadataText,
                    simDisplayName = simDisplayName,
                    onSimSelectorClick = onSimSelectorClick,
                )
            }
        }
    }
}
