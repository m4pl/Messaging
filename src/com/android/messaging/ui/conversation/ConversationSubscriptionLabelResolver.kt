package com.android.messaging.ui.conversation

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import com.android.messaging.R
import com.android.messaging.data.conversation.model.metadata.ConversationSubscriptionLabel

@Composable
internal fun ConversationSubscriptionLabel.resolveDisplayName(): String {
    return resolveDisplayName(resources = LocalResources.current)
}

internal fun ConversationSubscriptionLabel.resolveDisplayName(
    resources: Resources,
): String {
    return when (this) {
        is ConversationSubscriptionLabel.Named -> name

        is ConversationSubscriptionLabel.Slot -> {
            resources.getString(R.string.sim_slot_identifier, slotId.toString())
        }

        is ConversationSubscriptionLabel.DebugFake -> {
            resources.getString(R.string.debug_emulated_sim_display_name, slotId.toString())
        }
    }
}
