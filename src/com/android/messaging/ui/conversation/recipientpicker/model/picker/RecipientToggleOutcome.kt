package com.android.messaging.ui.conversation.recipientpicker.model.picker

internal sealed interface RecipientToggleOutcome {
    data object Added : RecipientToggleOutcome

    data object Removed : RecipientToggleOutcome

    data object OverLimit : RecipientToggleOutcome
}
