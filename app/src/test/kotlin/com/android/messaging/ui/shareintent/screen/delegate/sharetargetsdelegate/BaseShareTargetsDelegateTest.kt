package com.android.messaging.ui.shareintent.screen.delegate.sharetargetsdelegate

import com.android.messaging.data.contact.model.Contact
import com.android.messaging.data.contact.model.ContactDestination
import com.android.messaging.data.contact.model.ContactsPage
import com.android.messaging.data.contact.repository.ContactsRepository
import com.android.messaging.data.shareintent.model.ShareTargetConversation
import com.android.messaging.data.shareintent.repository.ShareTargetsRepository
import com.android.messaging.domain.contacts.usecase.IsReadContactsPermissionGranted
import com.android.messaging.ui.shareintent.screen.delegate.ShareTargetsDelegateImpl
import com.android.messaging.ui.shareintent.screen.mapper.ShareContactSectionMapperImpl
import com.android.messaging.ui.shareintent.screen.mapper.ShareContactUiStateMapper
import com.android.messaging.ui.shareintent.screen.mapper.ShareTargetUiStateMapper
import com.android.messaging.ui.shareintent.screen.model.ShareTargetUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
internal abstract class BaseShareTargetsDelegateTest {

    protected val repository = mockk<ShareTargetsRepository>()
    protected val contactsRepository = mockk<ContactsRepository>()
    protected val conversationMapper = mockk<ShareTargetUiStateMapper>()
    protected val contactMapper = mockk<ShareContactUiStateMapper>()
    protected val contactSectionMapper = ShareContactSectionMapperImpl()
    protected val isReadContactsPermissionGranted = mockk<IsReadContactsPermissionGranted>()

    @Before
    fun setUpDefaultStubs() {
        every { repository.observeShareTargets() } returns flowOf(persistentListOf())
        every { isReadContactsPermissionGranted() } returns true
        every { contactsRepository.searchContacts(any(), any()) } returns flowOf(emptyPage())
        every { conversationMapper.map(any()) } answers {
            firstArg<ImmutableList<ShareTargetConversation>>()
                .map(::conversationUiState)
                .toImmutableList()
        }
        every { contactMapper.map(any()) } answers {
            firstArg<ImmutableList<Contact>>()
                .map(::contactUiState)
                .toImmutableList()
        }
    }

    protected fun TestScope.createDelegate(): ShareTargetsDelegateImpl {
        return ShareTargetsDelegateImpl(
            repository = repository,
            contactsRepository = contactsRepository,
            conversationMapper = conversationMapper,
            contactMapper = contactMapper,
            contactSectionMapper = contactSectionMapper,
            isReadContactsPermissionGranted = isReadContactsPermissionGranted,
            defaultDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
        )
    }

    protected fun TestScope.settle() {
        testScheduler.advanceTimeBy(SETTLE_TIME_MILLIS)
        testScheduler.runCurrent()
    }

    protected fun givenRecents(conversations: List<ShareTargetConversation>) {
        every { repository.observeShareTargets() } returns flowOf(conversations.toImmutableList())
    }

    protected fun givenRecentsSource(): MutableSharedFlow<ImmutableList<ShareTargetConversation>> {
        val source = MutableSharedFlow<ImmutableList<ShareTargetConversation>>(
            replay = 1,
            extraBufferCapacity = 1,
        )
        every { repository.observeShareTargets() } returns source
        return source
    }

    protected fun givenContactsPages(pages: Map<Int, ContactsPage>) {
        every { contactsRepository.searchContacts(any(), any()) } answers {
            val offset = secondArg<Int>()
            flowOf(pages[offset] ?: emptyPage())
        }
    }

    protected fun shareTargetConversation(
        conversationId: String,
        name: String,
        normalizedDestination: String? = null,
        isGroup: Boolean = false,
        icon: String? = null,
    ): ShareTargetConversation {
        return ShareTargetConversation(
            conversationId = conversationId,
            name = name,
            icon = icon,
            normalizedDestination = normalizedDestination,
            isGroup = isGroup,
        )
    }

    protected fun contact(
        id: Long,
        displayName: String,
        destinationValue: String = "+1555000$id",
        normalizedValue: String = "+1555000$id",
    ): Contact {
        return Contact(
            id = id,
            lookupKey = "lookup_$id",
            displayName = displayName,
            photoUri = null,
            destinations = persistentListOf(
                ContactDestination(
                    dataId = id,
                    contactId = id,
                    value = destinationValue,
                    normalizedValue = normalizedValue,
                    displayValue = destinationValue,
                    kind = ContactDestination.Kind.PHONE,
                    type = 1,
                    customLabel = null,
                    isPrimary = false,
                    isSuperPrimary = false,
                ),
            ),
        )
    }

    protected fun contactsPage(
        contacts: List<Contact>,
        nextOffset: Int? = null,
    ): ContactsPage {
        return ContactsPage(
            contacts = contacts.toImmutableList(),
            nextOffset = nextOffset,
        )
    }

    protected fun emptyPage(): ContactsPage {
        return ContactsPage(
            contacts = persistentListOf(),
            nextOffset = null,
        )
    }

    private fun conversationUiState(
        conversation: ShareTargetConversation,
    ): ShareTargetUiState.Conversation {
        return ShareTargetUiState.Conversation(
            conversationId = conversation.conversationId,
            normalizedDestination = conversation.normalizedDestination,
            displayName = conversation.name,
            details = conversation.normalizedDestination,
            avatarUri = conversation.icon,
            isGroup = conversation.isGroup,
        )
    }

    private fun contactUiState(contact: Contact): ShareTargetUiState.Contact {
        val destination = contact.destinations.first()
        return ShareTargetUiState.Contact(
            contactId = contact.id,
            destination = destination.value,
            normalizedDestination = destination.normalizedValue,
            displayName = contact.displayName,
            details = destination.displayValue,
            avatarUri = contact.photoUri,
        )
    }

    private companion object {
        private const val SETTLE_TIME_MILLIS = 1_000L
    }
}
