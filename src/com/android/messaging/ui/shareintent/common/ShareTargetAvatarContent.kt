package com.android.messaging.ui.shareintent.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.messaging.ui.common.components.participantAvatarLabel
import com.android.messaging.ui.common.components.participantColorSeed
import com.android.messaging.ui.shareintent.screen.model.ShareTargetUiState

internal data class ShareTargetAvatarContent(
    val fallbackIcon: ImageVector,
    val fallbackLabel: String?,
    val colorSeedCode: String?,
)

internal fun ShareTargetUiState.avatarContent(): ShareTargetAvatarContent {
    val isGroup = this is ShareTargetUiState.Conversation && this.isGroup

    return ShareTargetAvatarContent(
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
