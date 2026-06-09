package com.android.messaging.data.conversationpicker.model

internal data class TargetConversation(
    val conversationId: String,
    val name: String,
    val icon: String?,
    val normalizedDestination: String?,
    val isGroup: Boolean,
)
