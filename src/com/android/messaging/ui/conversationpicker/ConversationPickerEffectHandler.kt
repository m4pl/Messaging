package com.android.messaging.ui.conversationpicker

import com.android.messaging.ui.conversationpicker.model.ConversationPickerEffect as Effect

internal interface ConversationPickerEffectHandler {
    fun handle(effect: Effect)
}
