package com.android.messaging.domain.conversationpicker.model

internal sealed interface SendContentResult {
    data object Success : SendContentResult
    data object Failure : SendContentResult
}
