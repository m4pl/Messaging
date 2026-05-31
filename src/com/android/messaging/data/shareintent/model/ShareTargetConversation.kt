package com.android.messaging.data.shareintent.model

internal data class ShareTargetConversation(
    val conversationId: String,
    val name: String,
    val icon: String?,
    val normalizedDestination: String?,
    val isGroup: Boolean,
)
