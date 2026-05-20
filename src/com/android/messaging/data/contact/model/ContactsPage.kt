package com.android.messaging.data.contact.model

import kotlinx.collections.immutable.ImmutableList

internal data class ContactsPage(
    val contacts: ImmutableList<Contact>,
    val nextOffset: Int?,
)
