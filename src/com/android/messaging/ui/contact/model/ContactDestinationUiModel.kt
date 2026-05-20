package com.android.messaging.ui.contact.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class ContactDestinationUiModel(
    val dataId: Long,
    val contactId: Long,
    val value: String,
    val normalizedValue: String,
    val displayValue: String,
    val kind: Kind,
    val type: Int,
    val customLabel: String?,
    val isPrimary: Boolean,
    val isSuperPrimary: Boolean,
) {
    enum class Kind {
        PHONE,
        EMAIL,
    }
}
