package com.android.messaging.ui.conversationpicker.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class ConversationPickerUiState(
    val targets: TargetsUiState = TargetsUiState(),
    val draft: DraftUiState = DraftUiState(),
    val isSendEnabled: Boolean = false,
) {
    val isLoading: Boolean
        get() = targets.isLoading || draft.isLoading
}
