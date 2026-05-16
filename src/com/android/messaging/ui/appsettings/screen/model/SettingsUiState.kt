package com.android.messaging.ui.appsettings.screen.model

import androidx.compose.runtime.Immutable
import com.android.messaging.ui.appsettings.general.model.AppSettingsUiState
import com.android.messaging.ui.appsettings.subscription.model.SubscriptionUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
internal data class SettingsUiState(
    val isMultiSim: Boolean? = null,
    val areSubscriptionsLoaded: Boolean = false,
    val subscriptionSettings: ImmutableList<SubscriptionUiState> = persistentListOf(),
    val appSettings: AppSettingsUiState = AppSettingsUiState(),
)
