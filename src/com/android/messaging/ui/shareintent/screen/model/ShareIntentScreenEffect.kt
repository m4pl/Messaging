package com.android.messaging.ui.shareintent.screen.model

internal sealed interface ShareIntentScreenEffect {

    data class OpenConversation(
        val conversationId: String,
    ) : ShareIntentScreenEffect

    data object CreateNewConversation : ShareIntentScreenEffect
}
