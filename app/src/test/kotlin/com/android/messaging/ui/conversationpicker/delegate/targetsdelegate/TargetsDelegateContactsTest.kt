package com.android.messaging.ui.conversationpicker.delegate.targetsdelegate

import com.android.messaging.ui.conversationpicker.model.TargetsUiState
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class TargetsDelegateContactsTest : BaseTargetsDelegateTest() {

    @Test
    fun bind_withoutContactsPermission_emitsNoPermissionAndNoSections() = runTest {
        every { isReadContactsPermissionGranted() } returns false

        val delegate = createDelegate()

        delegate.bind(backgroundScope)
        settle()

        val state = delegate.state.value
        assertFalse(state.contacts.hasPermission)
        assertTrue(state.contacts.sections.isEmpty())
        assertFalse(state.contacts.canLoadMore)
    }

    @Test
    fun bind_withPermission_loadsFirstPageIntoSections() = runTest {
        givenContactsPages(
            mapOf(
                0 to contactsPage(
                    listOf(contact(1L, "Alex"), contact(2L, "Brian")),
                ),
            ),
        )
        val delegate = createDelegate()

        delegate.bind(backgroundScope)
        settle()

        val state = delegate.state.value
        assertTrue(state.contacts.hasPermission)
        assertEquals(2, state.contactCount())
        assertFalse(state.contacts.canLoadMore)
    }

    @Test
    fun bind_withNextOffset_marksCanLoadMore() = runTest {
        givenContactsPages(
            mapOf(
                0 to contactsPage(
                    listOf(contact(1L, "Alex")),
                    nextOffset = 1,
                ),
            ),
        )
        val delegate = createDelegate()

        delegate.bind(backgroundScope)
        settle()

        assertTrue(delegate.state.value.contacts.canLoadMore)
    }

    @Test
    fun loadMoreContacts_appendsNextPageAndDeduplicatesByKey() = runTest {
        givenContactsPages(
            mapOf(
                0 to contactsPage(
                    listOf(contact(1L, "Alex"), contact(2L, "Brian")),
                    nextOffset = 2,
                ),
                2 to contactsPage(
                    listOf(contact(2L, "Brian"), contact(3L, "Cara")),
                ),
            ),
        )
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.loadMoreContacts()
        settle()

        val state = delegate.state.value
        assertEquals(3, state.contactCount())
        assertFalse(state.contacts.canLoadMore)
    }

    @Test
    fun loadMoreContacts_withoutNextOffset_isNoOp() = runTest {
        givenContactsPages(
            mapOf(
                0 to contactsPage(
                    listOf(contact(1L, "Alex")),
                ),
            ),
        )

        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.loadMoreContacts()
        settle()

        verify(exactly = 1) {
            contactsRepository.searchContacts(query = "", offset = 0)
        }
    }

    @Test
    fun loadMoreContacts_withoutPermission_isNoOp() = runTest {
        every { isReadContactsPermissionGranted() } returns false
        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.loadMoreContacts()
        settle()

        verify(exactly = 0) {
            contactsRepository.searchContacts(any(), any())
        }
    }

    @Test
    fun onContactsPermissionGranted_whenPreviouslyDenied_reloadsContacts() = runTest {
        every { isReadContactsPermissionGranted() } returns false

        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        every { isReadContactsPermissionGranted() } returns true

        givenContactsPages(
            mapOf(
                0 to contactsPage(
                    listOf(contact(1L, "Alex")),
                ),
            ),
        )
        delegate.onContactsPermissionGranted()
        settle()

        val state = delegate.state.value
        assertTrue(state.contacts.hasPermission)
        assertEquals(1, state.contactCount())
    }

    @Test
    fun onContactsPermissionGranted_whenAlreadyGranted_isNoOp() = runTest {
        givenContactsPages(
            mapOf(
                0 to contactsPage(
                    listOf(contact(1L, "Alex")),
                ),
            ),
        )

        val delegate = createDelegate()
        delegate.bind(backgroundScope)
        settle()

        delegate.onContactsPermissionGranted()
        settle()

        verify(exactly = 1) {
            contactsRepository.searchContacts(query = "", offset = 0)
        }
    }

    private fun TargetsUiState.contactCount(): Int {
        return contacts.sections.sumOf { it.targets.size }
    }
}
