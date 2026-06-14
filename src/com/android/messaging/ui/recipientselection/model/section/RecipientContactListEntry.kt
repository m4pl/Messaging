package com.android.messaging.ui.recipientselection.model.section

import androidx.compose.runtime.Immutable
import com.android.messaging.ui.recipientselection.model.picker.RecipientPickerListItem

@Immutable
internal sealed interface RecipientContactListEntry {
    val key: String

    @Immutable
    data class Header(
        val label: String,
    ) : RecipientContactListEntry {
        override val key: String = "section_header:$label"
    }

    @Immutable
    data class Row(
        val item: RecipientPickerListItem,
        val positionInSection: Int,
        val sectionSize: Int,
    ) : RecipientContactListEntry {
        override val key: String = item.id
    }
}
