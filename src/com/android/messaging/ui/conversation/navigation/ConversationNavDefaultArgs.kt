package com.android.messaging.ui.conversation.navigation

import android.os.Bundle

internal fun messageDetailsDefaultArgs(navKey: MessageDetailsNavKey): Bundle {
    return Bundle().apply {
        putString(CONVERSATION_ID_ARG, navKey.conversationId.value)
        putString(MESSAGE_ID_ARG, navKey.messageId.value)
    }
}

private const val CONVERSATION_ID_ARG = "conversationId"
private const val MESSAGE_ID_ARG = "messageId"
