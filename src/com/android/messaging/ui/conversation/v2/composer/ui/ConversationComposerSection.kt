package com.android.messaging.ui.conversation.v2.composer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.android.messaging.ui.conversation.v2.composer.model.ComposerAttachmentUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ConversationComposerSection(
    modifier: Modifier = Modifier,
    attachments: ImmutableList<ComposerAttachmentUiModel>,
    messageText: String,
    isMessageFieldEnabled: Boolean,
    isAttachmentActionEnabled: Boolean,
    isSendActionEnabled: Boolean,
    messageFieldFocusRequester: FocusRequester,
    onContactAttachClick: () -> Unit,
    onMediaPickerClick: () -> Unit,
    onMessageTextChange: (String) -> Unit,
    onPendingAttachmentRemove: (String) -> Unit,
    onResolvedAttachmentClick: (ComposerAttachmentUiModel.Resolved) -> Unit,
    onResolvedAttachmentRemove: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        ConversationAttachmentPreview(
            attachments = attachments,
            onPendingAttachmentRemove = onPendingAttachmentRemove,
            onResolvedAttachmentClick = onResolvedAttachmentClick,
            onResolvedAttachmentRemove = onResolvedAttachmentRemove,
        )

        ConversationComposeBar(
            messageText = messageText,
            isMessageFieldEnabled = isMessageFieldEnabled,
            isAttachmentActionEnabled = isAttachmentActionEnabled,
            isSendActionEnabled = isSendActionEnabled,
            messageFieldFocusRequester = messageFieldFocusRequester,
            onContactAttachClick = onContactAttachClick,
            onMediaPickerClick = onMediaPickerClick,
            onMessageTextChange = onMessageTextChange,
            onSendClick = onSendClick,
        )
    }
}
