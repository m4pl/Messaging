package com.android.messaging.ui.conversationpicker.host.share

import android.app.Activity
import com.android.messaging.data.conversation.model.draft.ConversationDraft
import com.android.messaging.datamodel.data.MessageData
import com.android.messaging.domain.conversationpicker.model.SendTarget
import com.android.messaging.domain.conversationpicker.usecase.BuildMessageDataFromDraft
import com.android.messaging.domain.conversationpicker.usecase.SendContentToTargets
import com.android.messaging.ui.UIIntents
import com.android.messaging.ui.conversationpicker.ConversationPickerEffectHandler
import com.android.messaging.ui.conversationpicker.model.ConversationPickerEffect as Effect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class ShareIntentEffectHandler(
    private val applicationScope: CoroutineScope,
    private val activity: Activity,
    private val draft: ConversationDraft?,
    private val sendContentToTargets: SendContentToTargets,
    private val buildMessageDataFromDraft: BuildMessageDataFromDraft,
) : ConversationPickerEffectHandler {

    private val messageData: MessageData? by lazy {
        draft?.let(buildMessageDataFromDraft::invoke)
    }

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.OpenConversation -> {
                openConversation(effect.conversationId)
            }

            is Effect.SendToSelected -> {
                sendToSelected(effect.targets, effect.draft)
            }
        }
    }

    private fun openConversation(conversationId: String) {
        UIIntents.get().launchConversationActivity(activity, conversationId, messageData)
        activity.finish()
    }

    private fun sendToSelected(
        targets: Set<SendTarget>,
        draft: ConversationDraft,
    ) {
        applicationScope.launch {
            sendContentToTargets(draft, targets)
        }

        UIIntents.get().launchConversationListActivity(activity)
        activity.finish()
    }
}
