package com.android.messaging.data.contact.model

import kotlinx.collections.immutable.ImmutableList

internal data class Contact(
    val id: Long,
    val lookupKey: String,
    val displayName: String,
    val photoUri: String?,
    val destinations: ImmutableList<ContactDestination>,
)
