package com.android.messaging.domain.conversationpicker.model

internal sealed interface SendTarget {

    data class Conversation(
        val conversationId: String,
    ) : SendTarget

    data class Contact(
        val destination: String,
    ) : SendTarget
}
