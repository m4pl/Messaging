package com.android.messaging.ui.host

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.android.messaging.ui.conversationlist.chats.ConversationListEffectHandlerImpl
import com.android.messaging.ui.conversationlist.chats.ConversationListScreen
import com.android.messaging.ui.navigation.ConversationListNavKey
import com.android.messaging.ui.navigation.OnboardingNavKey
import com.android.messaging.ui.onboarding.screen.OnboardingEffectHandlerImpl
import com.android.messaging.ui.onboarding.screen.OnboardingScreen

internal fun appNavEntryProvider(
    routeState: AppNavRouteState,
): (NavKey) -> NavEntry<NavKey> {
    return entryProvider {
        entry<ConversationListNavKey>(
            content = conversationListRouteContent(),
        )
        entry<OnboardingNavKey>(
            content = onboardingRouteContent(routeState = routeState),
        )
    }
}

private fun conversationListRouteContent(): @Composable (ConversationListNavKey) -> Unit {
    return {
        val activity = checkNotNull(LocalActivity.current)
        val hostView = LocalView.current
        val effectHandler = remember(activity, hostView) {
            ConversationListEffectHandlerImpl(
                activity = activity,
                hostView = hostView,
            )
        }

        ConversationListScreen(
            effectHandler = effectHandler,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun onboardingRouteContent(
    routeState: AppNavRouteState,
): @Composable (OnboardingNavKey) -> Unit {
    return {
        val activity = checkNotNull(LocalActivity.current)
        val roleManager = routeState.roleManager.value
        val effectHandler = remember(activity, roleManager) {
            OnboardingEffectHandlerImpl(
                activity = activity,
                roleManager = roleManager,
            )
        }

        OnboardingScreen(
            effectHandler = effectHandler,
            onNavigateBack = { routeState.onFinish.value() },
            onOnboardingComplete = {
                routeState.navigationReducer.value.reset(
                    backStack = routeState.backStack,
                    destination = ConversationListNavKey,
                )
                routeState.onOnboardingComplete.value()
            },
        )
    }
}
