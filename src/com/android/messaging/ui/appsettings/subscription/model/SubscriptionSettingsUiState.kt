package com.android.messaging.ui.appsettings.subscription.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class SubscriptionSettingsUiState(
    val isMultiSim: Boolean? = null,
    val isLoaded: Boolean = false,
    val subscriptions: ImmutableList<SubscriptionUiState> = persistentListOf(),
)
