package com.android.messaging.ui.conversationpicker.delegate.targetsdelegate

import com.android.messaging.data.contact.model.Contact
import com.android.messaging.data.contact.model.ContactDestination
import com.android.messaging.data.contact.model.ContactsPage
import com.android.messaging.data.contact.repository.ContactsRepository
import com.android.messaging.data.conversationpicker.model.TargetConversation
import com.android.messaging.data.conversationpicker.repository.TargetsRepository
import com.android.messaging.domain.contacts.usecase.IsReadContactsPermissionGranted
import com.android.messaging.ui.conversationpicker.delegate.TargetsDelegateImpl
import com.android.messaging.ui.conversationpicker.mapper.ContactSectionMapperImpl
import com.android.messaging.ui.conversationpicker.mapper.ContactUiStateMapper
import com.android.messaging.ui.conversationpicker.mapper.TargetUiStateMapper
import com.android.messaging.ui.conversationpicker.model.TargetUiState
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
internal abstract class BaseTargetsDelegateTest {

    protected val repository = mockk<TargetsRepository>()
    protected val contactsRepository = mockk<ContactsRepository>()
    protected val conversationMapper = mockk<TargetUiStateMapper>()
    protected val contactMapper = mockk<ContactUiStateMapper>()
    protected val contactSectionMapper = ContactSectionMapperImpl()
    protected val isReadContactsPermissionGranted = mockk<IsReadContactsPermissionGranted>()

    @Before
    fun setUpDefaultStubs() {
        every { repository.observeTargets() } returns flowOf(persistentListOf())
        every { isReadContactsPermissionGranted() } returns true
        every { contactsRepository.searchContacts(any(), any()) } returns flowOf(emptyPage())
        every { conversationMapper.map(any()) } answers {
            firstArg<ImmutableList<TargetConversation>>()
                .map(::conversationUiState)
                .toImmutableList()
        }
        every { contactMapper.map(any()) } answers {
            firstArg<ImmutableList<Contact>>()
                .map(::contactUiState)
                .toImmutableList()
        }
    }

    protected fun TestScope.createDelegate(): TargetsDelegateImpl {
        return TargetsDelegateImpl(
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

    protected fun givenRecents(conversations: List<TargetConversation>) {
        every { repository.observeTargets() } returns flowOf(conversations.toImmutableList())
    }

    protected fun givenRecentsSource(): MutableSharedFlow<ImmutableList<TargetConversation>> {
        val source = MutableSharedFlow<ImmutableList<TargetConversation>>(
            replay = 1,
            extraBufferCapacity = 1,
        )
        every { repository.observeTargets() } returns source
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
    ): TargetConversation {
        return TargetConversation(
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
        conversation: TargetConversation,
    ): TargetUiState.Conversation {
        return TargetUiState.Conversation(
            conversationId = conversation.conversationId,
            normalizedDestination = conversation.normalizedDestination,
            displayName = conversation.name,
            details = conversation.normalizedDestination,
            avatarUri = conversation.icon,
            isGroup = conversation.isGroup,
        )
    }

    private fun contactUiState(contact: Contact): TargetUiState.Contact {
        val destination = contact.destinations.first()
        return TargetUiState.Contact(
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
