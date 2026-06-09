package com.android.messaging.ui.conversationpicker.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class ContactSection(
    val label: String,
    val targets: ImmutableList<TargetUiState.Contact> = persistentListOf(),
)
