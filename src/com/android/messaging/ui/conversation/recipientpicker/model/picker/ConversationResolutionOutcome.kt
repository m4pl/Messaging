package com.android.messaging.ui.conversation.recipientpicker.model.picker

internal sealed interface ConversationResolutionOutcome {
    data class Resolved(
        val conversationId: String,
    ) : ConversationResolutionOutcome

    data object Failed : ConversationResolutionOutcome
}
