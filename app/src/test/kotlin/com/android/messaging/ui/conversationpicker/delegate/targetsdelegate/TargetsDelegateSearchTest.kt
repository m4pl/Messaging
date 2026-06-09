package com.android.messaging.ui.conversationpicker.delegate.targetsdelegate

import com.android.messaging.data.conversationpicker.model.TargetConversation
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class TargetsDelegateSearchTest : BaseTargetsDelegateTest() {

    @Test
    fun setSearchActive_isReflectedInState() = runTest {
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.setSearchActive(true)
        settle()

        assertTrue(delegate.state.value.isSearchActive)
    }

    @Test
    fun setSearchQuery_filtersRecentsByDisplayName() = runTest {
        givenRecents(
            listOf(
                shareTargetConversation(conversationId = "1", name = "Alice"),
                shareTargetConversation(conversationId = "2", name = "Bob"),
            ),
        )

        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.setSearchActive(true)
        delegate.setSearchQuery("ali")
        settle()

        val recents = delegate.state.value.recent.targets
        assertEquals(listOf("Alice"), recents.map { it.displayName })
    }

    @Test
    fun setSearchQuery_filtersRecentsByDetails() = runTest {
        givenRecents(
            listOf(
                shareTargetConversation(
                    conversationId = "1",
                    name = "Alice",
                    normalizedDestination = "+15551234",
                ),
                shareTargetConversation(
                    conversationId = "2",
                    name = "Bob",
                    normalizedDestination = "+15559999",
                ),
            ),
        )

        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.setSearchActive(true)
        delegate.setSearchQuery("1234")
        settle()

        val recents = delegate.state.value.recent.targets
        assertEquals(listOf("Alice"), recents.map { it.displayName })
    }

    @Test
    fun setSearchQuery_hidesLoadMoreAndCollapseAffordances() = runTest {
        givenRecents(recents(count = 8))

        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.setSearchActive(true)
        delegate.setSearchQuery("Conversation")
        settle()

        val state = delegate.state.value
        assertFalse(state.recent.canLoadMore)
        assertFalse(state.recent.canCollapse)
    }

    @Test
    fun setSearchActiveFalse_clearsQueryAndRestoresLimitedRecents() = runTest {
        givenRecents(recents(count = 8))

        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.setSearchActive(true)
        delegate.setSearchQuery("Conversation 7")
        settle()

        delegate.setSearchActive(false)
        settle()

        val state = delegate.state.value
        assertFalse(state.isSearchActive)
        assertEquals(5, state.recent.targets.size)
        assertTrue(state.recent.canLoadMore)
    }

    @Test
    fun setSearchQuery_requeriesContactsWithQuery() = runTest {
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.setSearchActive(true)
        delegate.setSearchQuery("alex")
        settle()

        verify {
            contactsRepository.searchContacts(query = "alex", offset = 0)
        }
    }

    private fun recents(count: Int): List<TargetConversation> {
        return (1..count).map { index ->
            shareTargetConversation(
                conversationId = index.toString(),
                name = "Conversation $index",
            )
        }
    }
}
