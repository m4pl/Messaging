package com.android.messaging.domain.conversationpicker.usecase

import com.android.messaging.data.conversation.model.draft.ConversationDraft
import com.android.messaging.domain.conversationpicker.model.SendTarget
import javax.inject.Inject

internal interface SendContentToTargets {
    suspend operator fun invoke(draft: ConversationDraft, targets: Set<SendTarget>)
}

internal class SendContentToTargetsImpl @Inject constructor(
    private val resolveTargetsToConversationIds: ResolveTargetsToConversationIds,
    private val sendContentToConversations: SendContentToConversations,
) : SendContentToTargets {

    override suspend fun invoke(
        draft: ConversationDraft,
        targets: Set<SendTarget>,
    ) {
        val conversationIds = resolveTargetsToConversationIds(targets)
        sendContentToConversations(draft, conversationIds)
    }
}
