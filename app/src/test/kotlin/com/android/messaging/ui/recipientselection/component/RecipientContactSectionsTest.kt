package com.android.messaging.ui.recipientselection.component

import com.android.messaging.ui.contact.model.ContactUiModel
import com.android.messaging.ui.recipientselection.model.picker.RecipientPickerListItem
import com.android.messaging.ui.recipientselection.model.section.RecipientContactListEntry
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test

internal class RecipientContactSectionsTest {

    @Test
    fun sectionLabel_usesUppercasedFirstLetter() {
        assertEquals("A", recipientContactSectionLabel(contactItem(id = 1L, name = "alex")))
        assertEquals("Z", recipientContactSectionLabel(contactItem(id = 2L, name = "  Zoe")))
    }

    @Test
    fun sectionLabel_bucketsNonLetterStartsUnderHash() {
        assertEquals("#", recipientContactSectionLabel(contactItem(id = 1L, name = "+1 555 0100")))
        assertEquals("#", recipientContactSectionLabel(contactItem(id = 2L, name = "#tag")))
        assertEquals("#", recipientContactSectionLabel(contactItem(id = 3L, name = "   ")))
    }

    @Test
    fun entries_withHeaders_groupsConsecutiveContactsBySection() {
        val items = persistentListOf(
            contactItem(id = 1L, name = "Alex"),
            contactItem(id = 2L, name = "Amelia"),
            contactItem(id = 3L, name = "Bob"),
        )

        val entries = recipientContactListEntries(
            items = items,
            showSectionHeaders = true,
        )

        assertEquals(
            listOf(
                "section_header:A",
                "contact:1",
                "contact:2",
                "section_header:B",
                "contact:3",
            ),
            entries.map { it.key },
        )

        val rows = entries.filterIsInstance<RecipientContactListEntry.Row>()
        assertEquals(listOf(0, 1, 0), rows.map { it.positionInSection })
        assertEquals(listOf(2, 2, 1), rows.map { it.sectionSize })
    }

    @Test
    fun entries_placesNonLetterSectionLastRegardlessOfInputOrder() {
        val items = persistentListOf(
            contactItem(id = 1L, name = "+1 555 0100"),
            contactItem(id = 2L, name = "Ada"),
            contactItem(id = 3L, name = "Bob"),
        )

        val entries = recipientContactListEntries(
            items = items,
            showSectionHeaders = true,
        )

        assertEquals(
            listOf(
                "section_header:A",
                "contact:2",
                "section_header:B",
                "contact:3",
                "section_header:#",
                "contact:1",
            ),
            entries.map { it.key },
        )
    }

    @Test
    fun entries_withHeaders_groupsEveryNonLetterContactUnderSingleHashSection() {
        val items = persistentListOf(
            contactItem(id = 1L, name = "+1 555 0100"),
            contactItem(id = 2L, name = "#hash"),
            contactItem(id = 3L, name = "42"),
        )

        val entries = recipientContactListEntries(
            items = items,
            showSectionHeaders = true,
        )

        assertEquals(
            listOf(
                "section_header:#",
                "contact:1",
                "contact:2",
                "contact:3",
            ),
            entries.map { it.key },
        )
    }

    @Test
    fun entries_withHeaders_returnsEmptyForEmptyInput() {
        val entries = recipientContactListEntries(
            items = persistentListOf(),
            showSectionHeaders = true,
        )

        assertEquals(emptyList<String>(), entries.map { it.key })
    }

    @Test
    fun entries_withoutHeaders_keepsFlatSingleSection() {
        val items = persistentListOf(
            contactItem(id = 1L, name = "Alex"),
            contactItem(id = 2L, name = "Bob"),
        )

        val entries = recipientContactListEntries(
            items = items,
            showSectionHeaders = false,
        )

        assertEquals(
            emptyList<RecipientContactListEntry.Header>(),
            entries.filterIsInstance<RecipientContactListEntry.Header>(),
        )

        val rows = entries.filterIsInstance<RecipientContactListEntry.Row>()
        assertEquals(listOf(0, 1), rows.map { it.positionInSection })
        assertEquals(listOf(2, 2), rows.map { it.sectionSize })
    }

    private fun contactItem(
        id: Long,
        name: String,
    ): RecipientPickerListItem.Contact {
        return RecipientPickerListItem.Contact(
            contact = ContactUiModel(
                id = id,
                lookupKey = "lookup:$id",
                displayName = name,
                photoUri = null,
                destinations = persistentListOf(),
            ),
        )
    }
}
