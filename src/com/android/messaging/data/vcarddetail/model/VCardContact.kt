package com.android.messaging.data.vcarddetail.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class VCardContact(
    val displayName: String?,
    val avatarUri: String?,
    val fields: ImmutableList<VCardField>,
)
