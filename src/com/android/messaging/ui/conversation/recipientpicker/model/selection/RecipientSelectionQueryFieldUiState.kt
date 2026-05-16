package com.android.messaging.ui.conversation.recipientpicker.model.selection

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.android.messaging.ui.conversation.recipientpicker.model.picker.SelectedRecipient
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class RecipientSelectionQueryFieldUiState(
    val query: String,
    val enabled: Boolean,
    val prefixText: String,
    val placeholderText: String,
    val selectedRecipients: ImmutableList<SelectedRecipient>,
)

@Immutable
internal data class RecipientSelectionQueryFieldPlacement(
    val modifier: Modifier = Modifier,
    val layout: RecipientSelectionQueryFieldLayout = RecipientSelectionQueryFieldLayout.FULL_WIDTH,
    val maxInlineWidth: Dp? = null,
)

internal enum class RecipientSelectionQueryFieldLayout {
    FULL_WIDTH,
    INLINE,
}
