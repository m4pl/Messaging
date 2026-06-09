package com.android.messaging.domain.conversationpicker.usecase

import com.android.messaging.domain.conversation.usecase.participant.ResolveConversationId
import com.android.messaging.domain.conversation.usecase.participant.model.ResolveConversationIdResult
import com.android.messaging.domain.conversationpicker.model.SendTarget
import javax.inject.Inject

internal interface ResolveTargetsToConversationIds {
    suspend operator fun invoke(targets: Set<SendTarget>): Set<String>
}

internal class ResolveTargetsToConversationIdsImpl @Inject constructor(
    private val resolveConversationId: ResolveConversationId,
) : ResolveTargetsToConversationIds {

    override suspend fun invoke(targets: Set<SendTarget>): Set<String> {
        return targets.mapNotNullTo(LinkedHashSet(targets.size)) { target ->
            resolveTarget(target)
        }
    }

    private suspend fun resolveTarget(target: SendTarget): String? {
        return when (target) {
            is SendTarget.Conversation -> target.conversationId
            is SendTarget.Contact -> resolveContactConversationId(target.destination)
        }
    }

    private suspend fun resolveContactConversationId(destination: String): String? {
        return when (val result = resolveConversationId(listOf(destination))) {
            is ResolveConversationIdResult.Resolved -> result.conversationId
            ResolveConversationIdResult.EmptyDestinations -> null
            ResolveConversationIdResult.NotResolved -> null
        }
    }
}
