package com.android.messaging.ui.conversationpicker.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class RecentTargetsUiState(
    val targets: ImmutableList<TargetUiState> = persistentListOf(),
    val canLoadMore: Boolean = false,
    val canCollapse: Boolean = false,
)
