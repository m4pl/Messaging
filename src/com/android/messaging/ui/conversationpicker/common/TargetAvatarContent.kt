package com.android.messaging.ui.conversationpicker.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.messaging.ui.common.components.participantAvatarLabel
import com.android.messaging.ui.common.components.participantColorSeed
import com.android.messaging.ui.conversationpicker.model.TargetUiState

internal data class TargetAvatarContent(
    val fallbackIcon: ImageVector,
    val fallbackLabel: String?,
    val colorSeedCode: String?,
)

internal fun TargetUiState.avatarContent(): TargetAvatarContent {
    val isGroup = this is TargetUiState.Conversation && this.isGroup

    return TargetAvatarContent(
        fallbackIcon = when {
            isGroup -> Icons.Default.Group
            else -> Icons.Default.Person
        },
        fallbackLabel = when {
            isGroup -> null
            else -> participantAvatarLabel(source = displayName)
        },
        colorSeedCode = participantColorSeed(normalizedDestination = normalizedDestination),
    )
}
