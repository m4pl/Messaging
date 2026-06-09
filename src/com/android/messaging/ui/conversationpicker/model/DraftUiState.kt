package com.android.messaging.ui.conversationpicker.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class DraftUiState(
    val isLoading: Boolean = true,
    val isReviewing: Boolean = false,
    val text: String = "",
    val subjectText: String = "",
    val attachments: ImmutableList<AttachmentUiModel> = persistentListOf(),
)
