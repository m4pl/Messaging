package com.android.messaging.ui.conversationpicker.mapper

import com.android.messaging.data.contact.model.Contact
import com.android.messaging.ui.conversationpicker.formatter.TargetTextFormatter
import com.android.messaging.ui.conversationpicker.model.TargetUiState
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal interface ContactUiStateMapper {
    fun map(contacts: ImmutableList<Contact>): ImmutableList<TargetUiState.Contact>
}

internal class ContactUiStateMapperImpl @Inject constructor(
    private val textFormatter: TargetTextFormatter,
) : ContactUiStateMapper {

    override fun map(
        contacts: ImmutableList<Contact>,
    ): ImmutableList<TargetUiState.Contact> {
        return contacts
            .mapNotNull(::toContactUiState)
            .toImmutableList()
    }

    private fun toContactUiState(contact: Contact): TargetUiState.Contact? {
        val destination = contact.destinations.firstOrNull() ?: return null
        val name = contact.displayName.ifBlank { destination.displayValue }

        return TargetUiState.Contact(
            contactId = contact.id,
            destination = destination.value,
            normalizedDestination = destination.normalizedValue,
            displayName = textFormatter.wrap(name),
            details = textFormatter.detailsOrNull(
                name = name,
                value = destination.displayValue,
            ),
            avatarUri = contact.photoUri,
        )
    }
}
