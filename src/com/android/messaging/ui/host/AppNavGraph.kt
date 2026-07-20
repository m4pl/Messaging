package com.android.messaging.ui.host

import android.app.role.RoleManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.android.messaging.ui.navigation.AppNavDisplay
import com.android.messaging.ui.navigation.NavigationReducer
import com.android.messaging.ui.navigation.NavigationReducerImpl

@Composable
internal fun AppNavGraph(
    startDestination: NavKey,
    roleManager: RoleManager,
    onOnboardingComplete: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    navigationReducer: NavigationReducer = defaultAppNavReducer,
) {
    val backStack = rememberNavBackStack(startDestination)
    val routeState = AppNavRouteState(
        backStack = backStack,
        navigationReducer = rememberUpdatedState(newValue = navigationReducer),
        onFinish = rememberUpdatedState(newValue = onFinish),
        onOnboardingComplete = rememberUpdatedState(newValue = onOnboardingComplete),
        roleManager = rememberUpdatedState(newValue = roleManager),
    )
    val entryProvider = remember(backStack) {
        appNavEntryProvider(routeState = routeState)
    }

    AppNavDisplay(
        backStack = backStack,
        entryProvider = entryProvider,
        onBack = {
            if (!navigationReducer.pop(backStack = backStack)) {
                onFinish()
            }
        },
        modifier = modifier,
    )
}

private val defaultAppNavReducer: NavigationReducer = NavigationReducerImpl()
