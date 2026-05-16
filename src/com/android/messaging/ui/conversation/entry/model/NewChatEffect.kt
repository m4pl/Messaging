package com.android.messaging.ui.conversation.entry.model

internal sealed interface NewChatEffect {

    data class NavigateToConversation(
        val conversationId: String,
        val selfParticipantId: String?,
    ) : NewChatEffect

    data object NavigateBack : NewChatEffect

    data class ShowMessage(
        val messageResId: Int,
    ) : NewChatEffect
}
