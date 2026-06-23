package com.android.messaging.ui.conversationlist

import com.android.messaging.ui.conversationlist.model.ConversationListEffect as Effect

internal interface ConversationListEffectHandler {
    fun handle(effect: Effect)
}
