package com.android.messaging.ui.shareintent.screen.delegate.sharetargetsdelegate

import app.cash.turbine.test
import com.android.messaging.ui.shareintent.screen.model.ShareTargetUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ShareTargetsDelegateSelectionTest : BaseShareTargetsDelegateTest() {

    @Test
    fun toggleSelection_addsTarget() = runTest {
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.toggleSelection(
            conversationTarget(
                id = "1",
                normalizedDestination = "+15550001",
            ),
        )

        val selection = delegate.state.value.selection
        assertEquals(setOf("dest:+15550001"), selection.selectedIds)
        assertEquals(1, selection.selectedTargets.size)
    }

    @Test
    fun toggleSelection_calledTwiceForSameTarget_removesIt() = runTest {
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        val target = conversationTarget(
            id = "1",
            normalizedDestination = "+15550001",
        )
        delegate.toggleSelection(target)
        delegate.toggleSelection(target)

        assertTrue(delegate.state.value.selection.selectedIds.isEmpty())
    }

    @Test
    fun toggleSelection_dedupesAcrossConversationAndContactWithSameDestination() = runTest {
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.toggleSelection(
            conversationTarget(
                id = "1",
                normalizedDestination = "+15550001",
            ),
        )
        delegate.toggleSelection(
            contactTarget(
                contactId = 9L,
                normalizedDestination = "+15550001",
            ),
        )

        assertTrue(delegate.state.value.selection.selectedIds.isEmpty())
    }

    @Test
    fun toggleSelection_usesConversationKeyWhenDestinationIsNull() = runTest {
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.toggleSelection(
            conversationTarget(
                id = "7",
                normalizedDestination = null,
            ),
        )

        assertEquals(setOf("conversation:7"), delegate.state.value.selection.selectedIds)
    }

    @Test
    fun clearSelection_removesAllSelectedTargets() = runTest {
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.toggleSelection(
            conversationTarget(
                id = "1",
                normalizedDestination = "+15550001",
            ),
        )
        delegate.clearSelection()

        assertTrue(delegate.state.value.selection.selectedIds.isEmpty())
    }

    @Test
    fun currentSelectedTargets_returnsSelectedTargets() = runTest {
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        val target = conversationTarget(
            id = "1",
            normalizedDestination = "+15550001",
        )
        delegate.toggleSelection(target)

        assertEquals(listOf(target), delegate.currentSelectedTargets())
    }

    @Test
    fun selectedIds_flowEmitsSelectionIds() = runTest {
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.toggleSelection(
            conversationTarget(
                id = "1",
                normalizedDestination = "+15550001",
            ),
        )

        delegate.selectedIds.test {
            assertEquals(setOf("dest:+15550001"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun recentsChange_prunesConversationNoLongerAvailable() = runTest {
        val source = givenRecentsSource()
        val delegate = createDelegate()
        delegate.bind(backgroundScope)

        source.emit(
            persistentListOf(
                shareTargetConversation(
                    conversationId = "1",
                    name = "One",
                ),
                shareTargetConversation(
                    conversationId = "2",
                    name = "Two",
                ),
            ),
        )
        settle()

        delegate.toggleSelection(
            conversationTarget(
                id = "1",
                normalizedDestination = null,
            ),
        )
        source.emit(
            persistentListOf(
                shareTargetConversation(
                    conversationId = "2",
                    name = "Two",
                ),
            ),
        )
        settle()

        assertTrue(delegate.state.value.selection.selectedIds.isEmpty())
    }

    @Test
    fun recentsChange_keepsConversationStillAvailable() = runTest {
        val source = givenRecentsSource()
        val delegate = createDelegate()
        delegate.bind(backgroundScope)

        source.emit(
            persistentListOf(
                shareTargetConversation(
                    conversationId = "1",
                    name = "One",
                ),
            ),
        )
        settle()

        delegate.toggleSelection(
            conversationTarget(
                id = "1",
                normalizedDestination = null,
            ),
        )
        source.emit(
            persistentListOf(
                shareTargetConversation(
                    conversationId = "1",
                    name = "One",
                ),
            ),
        )
        settle()

        assertEquals(setOf("conversation:1"), delegate.state.value.selection.selectedIds)
    }

    @Test
    fun recentsChange_keepsSelectedContact() = runTest {
        val source = givenRecentsSource()
        val delegate = createDelegate()
        delegate.bind(backgroundScope)

        source.emit(
            persistentListOf(
                shareTargetConversation(
                    conversationId = "1",
                    name = "One",
                ),
            ),
        )
        settle()

        delegate.toggleSelection(
            contactTarget(
                contactId = 9L,
                normalizedDestination = "+15550009",
            ),
        )
        source.emit(persistentListOf())
        settle()

        assertEquals(setOf("dest:+15550009"), delegate.state.value.selection.selectedIds)
    }

    private fun conversationTarget(
        id: String,
        normalizedDestination: String?,
    ): ShareTargetUiState.Conversation {
        return ShareTargetUiState.Conversation(
            conversationId = id,
            normalizedDestination = normalizedDestination,
            displayName = "Conversation $id",
            details = null,
            avatarUri = null,
            isGroup = false,
        )
    }

    private fun contactTarget(
        contactId: Long,
        normalizedDestination: String,
    ): ShareTargetUiState.Contact {
        return ShareTargetUiState.Contact(
            contactId = contactId,
            destination = normalizedDestination,
            normalizedDestination = normalizedDestination,
            displayName = "Contact $contactId",
            details = null,
            avatarUri = null,
        )
    }
}
