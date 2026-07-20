package com.android.messaging.ui.host

import android.app.role.RoleManager
import androidx.compose.runtime.State
import androidx.navigation3.runtime.NavKey
import com.android.messaging.ui.navigation.NavigationReducer

internal class AppNavRouteState(
    val backStack: MutableList<NavKey>,
    val navigationReducer: State<NavigationReducer>,
    val onFinish: State<() -> Unit>,
    val onOnboardingComplete: State<() -> Unit>,
    val roleManager: State<RoleManager>,
)
