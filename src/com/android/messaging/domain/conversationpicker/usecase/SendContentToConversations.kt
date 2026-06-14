package com.android.messaging.domain.conversationpicker.usecase

import androidx.core.net.toUri
import com.android.messaging.data.conversation.model.draft.ConversationDraft
import com.android.messaging.data.conversation.model.draft.ConversationDraftAttachment
import com.android.messaging.di.core.IoDispatcher
import com.android.messaging.domain.conversation.usecase.draft.SendConversationDraft
import com.android.messaging.domain.conversation.usecase.draft.exception.SendConversationDraftException
import com.android.messaging.domain.conversationpicker.model.SendContentResult
import com.android.messaging.util.LogUtil
import com.android.messaging.util.UriUtil
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

internal interface SendContentToConversations {
    suspend operator fun invoke(
        draft: ConversationDraft,
        conversationIds: Set<String>,
    ): SendContentResult
}

internal class SendContentToConversationsImpl @Inject constructor(
    private val sendConversationDraft: SendConversationDraft,
    @param:IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : SendContentToConversations {

    override suspend fun invoke(
        draft: ConversationDraft,
        conversationIds: Set<String>,
    ): SendContentResult {
        if (conversationIds.isEmpty()) {
            return SendContentResult.Success
        }

        val drafts = perConversationDrafts(
            draft = draft,
            count = conversationIds.size,
        )

        var anyFailed = false
        conversationIds.forEachIndexed { index, conversationId ->
            if (!sendToConversation(conversationId, drafts[index])) {
                anyFailed = true
            }
        }

        return when {
            anyFailed -> SendContentResult.Failure
            else -> SendContentResult.Success
        }
    }

    private suspend fun perConversationDrafts(
        draft: ConversationDraft,
        count: Int,
    ): List<ConversationDraft> {
        if (draft.attachments.isEmpty()) {
            return List(count) { draft }
        }

        return withContext(ioDispatcher) {
            List(count) { index ->
                when (index) {
                    0 -> draft
                    else -> draft.copy(
                        attachments = copyAttachments(draft.attachments),
                    )
                }
            }
        }
    }

    private fun copyAttachments(
        attachments: ImmutableList<ConversationDraftAttachment>,
    ): ImmutableList<ConversationDraftAttachment> {
        return attachments.mapNotNull { attachment ->
            UriUtil.persistContentToScratchSpace(
                attachment.contentUri.toUri(),
            )?.let {
                attachment.copy(contentUri = it.toString())
            }
        }.toImmutableList()
    }

    private suspend fun sendToConversation(
        conversationId: String,
        draft: ConversationDraft,
    ): Boolean {
        return try {
            sendConversationDraft(conversationId, draft).collect()
            true
        } catch (exception: SendConversationDraftException) {
            LogUtil.w(TAG, "Failed to send shared draft to $conversationId", exception)
            false
        }
    }

    private companion object {
        private const val TAG = "SendContent"
    }
}
