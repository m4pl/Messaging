package com.android.messaging.ui.shareintent.screen

import android.app.Activity
import com.android.messaging.datamodel.data.MessageData
import com.android.messaging.ui.UIIntents
import com.android.messaging.ui.shareintent.screen.model.ShareIntentScreenEffect as Effect

internal interface ShareIntentEffectHandler {
    fun handle(effect: Effect)
}

internal class ShareIntentEffectHandlerImpl(
    private val activity: Activity,
    private val draft: MessageData?,
) : ShareIntentEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.OpenConversation -> {
                UIIntents.get().launchConversationActivity(
                    activity,
                    effect.conversationId,
                    draft,
                )
                activity.finish()
            }

            Effect.CreateNewConversation -> {
                UIIntents.get().launchCreateNewConversationActivity(activity, draft)
                activity.finish()
            }
        }
    }
}
