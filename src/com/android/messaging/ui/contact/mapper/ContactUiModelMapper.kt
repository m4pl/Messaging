package com.android.messaging.ui.contact.mapper

import com.android.messaging.data.contact.model.Contact
import com.android.messaging.data.contact.model.ContactDestination
import com.android.messaging.ui.contact.model.ContactDestinationUiModel
import com.android.messaging.ui.contact.model.ContactUiModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList

internal interface ContactUiModelMapper {
    fun map(contact: Contact): ContactUiModel
}

internal class ContactUiModelMapperImpl @Inject constructor() : ContactUiModelMapper {

    override fun map(contact: Contact): ContactUiModel {
        return ContactUiModel(
            id = contact.id,
            lookupKey = contact.lookupKey,
            displayName = contact.displayName,
            photoUri = contact.photoUri,
            destinations = contact
                .destinations
                .map(::mapDestination)
                .toImmutableList(),
        )
    }

    private fun mapDestination(destination: ContactDestination): ContactDestinationUiModel {
        return ContactDestinationUiModel(
            dataId = destination.dataId,
            contactId = destination.contactId,
            value = destination.value,
            normalizedValue = destination.normalizedValue,
            displayValue = destination.displayValue,
            kind = mapKind(destination.kind),
            type = destination.type,
            customLabel = destination.customLabel,
            isPrimary = destination.isPrimary,
            isSuperPrimary = destination.isSuperPrimary,
        )
    }

    private fun mapKind(kind: ContactDestination.Kind): ContactDestinationUiModel.Kind {
        return when (kind) {
            ContactDestination.Kind.PHONE -> ContactDestinationUiModel.Kind.PHONE
            ContactDestination.Kind.EMAIL -> ContactDestinationUiModel.Kind.EMAIL
        }
    }
}
