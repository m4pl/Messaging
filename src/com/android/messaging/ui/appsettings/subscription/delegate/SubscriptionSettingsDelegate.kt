package com.android.messaging.ui.appsettings.subscription.delegate

import com.android.messaging.data.subscriptionsettings.repository.SubscriptionSettingsRepository
import com.android.messaging.ui.appsettings.common.SettingsScreenDelegate
import com.android.messaging.ui.appsettings.subscription.mapper.SubscriptionSettingsUiStateMapper
import com.android.messaging.ui.appsettings.subscription.model.SubscriptionSettingsUiState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal interface SubscriptionSettingsDelegate :
    SettingsScreenDelegate<SubscriptionSettingsUiState> {
    fun onAutoRetrieveMmsChanged(subId: Int, enabled: Boolean)
    fun onAutoRetrieveMmsWhenRoamingChanged(subId: Int, enabled: Boolean)
    fun onDeliveryReportsChanged(subId: Int, enabled: Boolean)
    fun onGroupMmsChanged(subId: Int, enabled: Boolean)
    fun onPhoneNumberChanged(subId: Int, phoneNumber: String)
}

internal class SubscriptionSettingsDelegateImpl @Inject constructor(
    private val repository: SubscriptionSettingsRepository,
    private val mapper: SubscriptionSettingsUiStateMapper,
) : SubscriptionSettingsDelegate {

    private val _state = MutableStateFlow(
        SubscriptionSettingsUiState(
            isMultiSim = repository.isMultiSim(),
        ),
    )
    override val state: StateFlow<SubscriptionSettingsUiState> = _state.asStateFlow()

    private val refreshTriggers: Channel<Unit> = Channel(Channel.CONFLATED)

    private var boundScope: CoroutineScope? = null

    override fun bind(scope: CoroutineScope) {
        if (boundScope != null) {
            return
        }

        boundScope = scope

        scope.launch {
            merge(
                repository.observeSubscriptionsChanged(),
                refreshTriggers.receiveAsFlow(),
            )
                .onStart { emit(Unit) }
                .conflate()
                .map {
                    val data = repository.getSubscriptionSettings()
                    mapper.map(data)
                }
                .collect { _state.value = it }
        }
    }

    override fun refresh() {
        refreshTriggers.trySend(Unit)
    }

    override fun onAutoRetrieveMmsChanged(
        subId: Int,
        enabled: Boolean,
    ) {
        boundScope?.launch {
            repository.setAutoRetrieveMms(
                subId = subId,
                enabled = enabled,
            )
            refresh()
        }
    }

    override fun onAutoRetrieveMmsWhenRoamingChanged(
        subId: Int,
        enabled: Boolean,
    ) {
        boundScope?.launch {
            repository.setAutoRetrieveMmsWhenRoaming(
                subId = subId,
                enabled = enabled,
            )
            refresh()
        }
    }

    override fun onDeliveryReportsChanged(
        subId: Int,
        enabled: Boolean,
    ) {
        boundScope?.launch {
            repository.setDeliveryReportsEnabled(
                subId = subId,
                enabled = enabled,
            )
            refresh()
        }
    }

    override fun onGroupMmsChanged(
        subId: Int,
        enabled: Boolean,
    ) {
        boundScope?.launch {
            repository.setGroupMmsEnabled(
                subId = subId,
                enabled = enabled,
            )
            refresh()
        }
    }

    override fun onPhoneNumberChanged(
        subId: Int,
        phoneNumber: String,
    ) {
        boundScope?.launch {
            repository.setPhoneNumber(
                subId = subId,
                phoneNumber = phoneNumber,
            )
            refresh()
        }
    }
}
