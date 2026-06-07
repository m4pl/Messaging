package com.android.messaging.ui.shareintent.screen.delegate.sharedraftdelegate

import com.android.messaging.data.conversation.model.draft.ConversationDraft
import com.android.messaging.data.conversation.model.draft.ConversationDraftAttachment
import com.android.messaging.ui.shareintent.screen.delegate.ShareDraftDelegateImpl
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
internal abstract class BaseShareDraftDelegateTest {

    protected fun TestScope.createDelegate(): ShareDraftDelegateImpl {
        return ShareDraftDelegateImpl(
            defaultDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
        )
    }

    protected fun TestScope.boundDelegate(
        selectedIds: Flow<ImmutableSet<String>> = emptyFlow(),
    ): ShareDraftDelegateImpl {
        return createDelegate().also { delegate ->
            delegate.bind(backgroundScope, selectedIds)
            testScheduler.runCurrent()
        }
    }

    protected fun TestScope.settle() {
        testScheduler.runCurrent()
    }

    protected fun conversationDraft(
        messageText: String = "",
        subjectText: String = "",
        attachments: ImmutableList<ConversationDraftAttachment> = persistentListOf(),
    ): ConversationDraft {
        return ConversationDraft(
            messageText = messageText,
            subjectText = subjectText,
            attachments = attachments,
        )
    }

    protected fun draftAttachment(
        contentUri: String,
        contentType: String = "image/jpeg",
        durationMillis: Long? = null,
        displayName: String? = null,
    ): ConversationDraftAttachment {
        return ConversationDraftAttachment(
            contentType = contentType,
            contentUri = contentUri,
            durationMillis = durationMillis,
            displayName = displayName,
        )
    }
}
