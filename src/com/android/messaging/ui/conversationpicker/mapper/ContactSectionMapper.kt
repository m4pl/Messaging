package com.android.messaging.ui.conversationpicker.mapper

import com.android.messaging.ui.conversationpicker.model.ContactSection
import com.android.messaging.ui.conversationpicker.model.TargetUiState
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal interface ContactSectionMapper {
    fun map(
        contacts: ImmutableList<TargetUiState.Contact>,
    ): ImmutableList<ContactSection>
}

internal class ContactSectionMapperImpl @Inject constructor() : ContactSectionMapper {

    override fun map(
        contacts: ImmutableList<TargetUiState.Contact>,
    ): ImmutableList<ContactSection> {
        return contacts
            .groupBy(::sectionLabel)
            .map { (label, sectionContacts) ->
                ContactSection(
                    label = label,
                    targets = sectionContacts.toImmutableList(),
                )
            }
            .toImmutableList()
    }

    private fun sectionLabel(contact: TargetUiState.Contact): String {
        val firstCharacter = contact.displayName.trim().firstOrNull()?.uppercaseChar()

        return when {
            firstCharacter?.isLetter() == true -> firstCharacter.toString()
            else -> NON_LETTER_SECTION_LABEL
        }
    }

    private companion object {
        private const val NON_LETTER_SECTION_LABEL = "#"
    }
}
