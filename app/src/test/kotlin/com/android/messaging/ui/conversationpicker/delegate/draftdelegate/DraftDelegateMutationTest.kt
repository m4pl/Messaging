package com.android.messaging.ui.conversationpicker.delegate.draftdelegate

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class DraftDelegateMutationTest : BaseDraftDelegateTest() {

    @Test
    fun setDraftText_updatesText() = runTest {
        val delegate = createDelegate()

        delegate.setDraftText("hello")

        assertEquals("hello", delegate.currentDraft().messageText)
    }

    @Test
    fun clearDraftSubject_clearsSubject() = runTest {
        val delegate = createDelegate()

        delegate.resolveDraft(conversationDraft(subjectText = "subject"))
        delegate.clearDraftSubject()

        assertEquals("", delegate.currentDraft().subjectText)
    }

    @Test
    fun removeDraftAttachment_removesMatchingByContentUri() = runTest {
        val delegate = createDelegate()

        delegate.resolveDraft(
            conversationDraft(
                attachments = persistentListOf(
                    draftAttachment(contentUri = "content://a"),
                    draftAttachment(contentUri = "content://b"),
                ),
            ),
        )
        delegate.removeDraftAttachment("content://a")

        assertEquals(
            listOf("content://b"),
            delegate.currentDraft().attachments.map { it.contentUri },
        )
    }

    @Test
    fun removeDraftAttachment_withUnknownId_keepsAttachments() = runTest {
        val delegate = createDelegate()

        delegate.resolveDraft(
            conversationDraft(
                attachments = persistentListOf(
                    draftAttachment(contentUri = "content://a"),
                ),
            ),
        )
        delegate.removeDraftAttachment("content://missing")

        assertEquals(
            listOf("content://a"),
            delegate.currentDraft().attachments.map { it.contentUri },
        )
    }

    @Test
    fun state_mapsDraftAttachmentsToUiModelsInOrder() = runTest {
        val delegate = boundDelegate()

        delegate.resolveDraft(
            conversationDraft(
                attachments = persistentListOf(
                    draftAttachment(contentUri = "content://a"),
                    draftAttachment(contentUri = "content://b"),
                ),
            ),
        )
        settle()

        assertEquals(
            listOf("content://a", "content://b"),
            delegate.state.value.attachments.map { it.id },
        )
    }

    @Test
    fun state_reflectsRemovedAttachment() = runTest {
        val delegate = boundDelegate()

        delegate.resolveDraft(
            conversationDraft(
                attachments = persistentListOf(
                    draftAttachment(contentUri = "content://a"),
                    draftAttachment(contentUri = "content://b"),
                ),
            ),
        )
        delegate.removeDraftAttachment("content://a")
        settle()

        assertEquals(listOf("content://b"), delegate.state.value.attachments.map { it.id })
    }
}
