package com.android.messaging.ui.conversationpicker.delegate.draftdelegate

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class DraftDelegateResolveTest : BaseDraftDelegateTest() {

    @Test
    fun initialState_isLoadingWithEmptyDraft() = runTest {
        val delegate = boundDelegate()

        val state = delegate.state.value
        assertTrue(state.isLoading)
        assertFalse(state.isReviewing)
        assertEquals("", state.text)
        assertEquals("", state.subjectText)
        assertTrue(state.attachments.isEmpty())
    }

    @Test
    fun resolveDraft_withDraft_appliesDraftAndStopsLoading() = runTest {
        val delegate = boundDelegate()

        delegate.resolveDraft(
            conversationDraft(
                messageText = "shared text",
                subjectText = "subject"
            ),
        )
        settle()

        val state = delegate.state.value
        assertFalse(state.isLoading)
        assertEquals("shared text", state.text)
        assertEquals("subject", state.subjectText)
    }

    @Test
    fun resolveDraft_withNull_keepsEmptyDraftAndStopsLoading() = runTest {
        val delegate = boundDelegate()

        delegate.resolveDraft(null)
        settle()

        val state = delegate.state.value
        assertFalse(state.isLoading)
        assertEquals("", state.text)
        assertEquals("", state.subjectText)
        assertTrue(state.attachments.isEmpty())
    }

    @Test
    fun resolveDraft_whenAlreadyResolved_isIgnored() = runTest {
        val delegate = createDelegate()

        delegate.resolveDraft(conversationDraft(messageText = "first"))
        delegate.resolveDraft(conversationDraft(messageText = "second"))

        assertEquals("first", delegate.currentDraft().messageText)
    }
}
