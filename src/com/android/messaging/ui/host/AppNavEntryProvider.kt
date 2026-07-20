package com.android.messaging.ui.host

import android.app.role.RoleManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.android.messaging.ui.conversation.navigation.conversationEntries
import com.android.messaging.ui.conversation.navigation.rememberConversationNavigator
import com.android.messaging.ui.conversationlist.chats.ConversationListEffectHandlerImpl
import com.android.messaging.ui.conversationlist.chats.ConversationListScreen
import com.android.messaging.ui.navigation.ConversationListNavKey
import com.android.messaging.ui.navigation.LocalNavigator
import com.android.messaging.ui.navigation.OnboardingNavKey
import com.android.messaging.ui.onboarding.screen.OnboardingEffectHandlerImpl
import com.android.messaging.ui.onboarding.screen.OnboardingScreen

internal fun appNavEntryProvider(
    roleManager: RoleManager,
    onOnboardingComplete: () -> Unit,
): (NavKey) -> NavEntry<NavKey> {
    return entryProvider {
        entry<ConversationListNavKey>(
            content = conversationListRouteContent(),
        )
        entry<OnboardingNavKey>(
            content = onboardingRouteContent(
                roleManager = roleManager,
                onOnboardingComplete = onOnboardingComplete,
            ),
        )
        conversationEntries()
    }
}

private fun conversationListRouteContent(): @Composable (ConversationListNavKey) -> Unit {
    return {
        val activity = checkNotNull(LocalActivity.current)
        val hostView = LocalView.current
        val navigator = rememberConversationNavigator()
        val effectHandler = remember(activity, hostView) {
            ConversationListEffectHandlerImpl(
                activity = activity,
                hostView = hostView,
            )
        }

        ConversationListScreen(
            effectHandler = effectHandler,
            onNavigateToConversation = { conversationId ->
                navigator.navigateToConversation(conversationId = conversationId)
            },
            onNavigateToNewChat = navigator::navigateToNewChat,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun onboardingRouteContent(
    roleManager: RoleManager,
    onOnboardingComplete: () -> Unit,
): @Composable (OnboardingNavKey) -> Unit {
    return {
        val activity = checkNotNull(LocalActivity.current)
        val navigator = LocalNavigator.current
        val effectHandler = remember(activity, roleManager) {
            OnboardingEffectHandlerImpl(
                activity = activity,
                roleManager = roleManager,
            )
        }

        OnboardingScreen(
            effectHandler = effectHandler,
            onNavigateBack = navigator::back,
            onOnboardingComplete = {
                navigator.reset(destinations = listOf(ConversationListNavKey))
                onOnboardingComplete()
            },
        )
    }
}
