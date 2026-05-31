package com.android.messaging.ui.shareintent.screen.model

internal sealed interface ShareIntentAction {

    data class TargetClicked(
        val conversationId: String,
    ) : ShareIntentAction

    data object NewMessageClicked : ShareIntentAction
}
