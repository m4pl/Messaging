package com.android.messaging.ui.contact.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class ContactUiModel(
    val id: Long,
    val lookupKey: String,
    val displayName: String,
    val photoUri: String?,
    val destinations: ImmutableList<ContactDestinationUiModel>,
)
