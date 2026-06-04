package com.android.messaging.ui.conversation.recipientpicker.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.messaging.R
import com.android.messaging.ui.common.components.ParticipantAvatar
import com.android.messaging.ui.common.components.participantAvatarLabel
import com.android.messaging.ui.common.components.participantColorSeed
import com.android.messaging.ui.conversation.preview.previewRecipientPickerUiState
import com.android.messaging.ui.conversation.recipientpicker.model.picker.RecipientPickerListItem
import com.android.messaging.ui.core.MessagingPreviewColumn

private val RECIPIENT_SELECTION_AVATAR_SIZE = 40.dp

@Composable
internal fun RecipientSelectionContactAvatar(
    item: RecipientPickerListItem,
    isSelected: Boolean,
) {
    val avatarScale by rememberRecipientSelectionContactAvatarScale(
        isSelected = isSelected,
    )

    val displayName = recipientSelectionItemPrimaryText(item = item)

    AnimatedContent(
        targetState = isSelected,
        transitionSpec = {
            (
                fadeIn(
                    animationSpec = tween(durationMillis = 200),
                ) + scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    initialScale = 0.8f,
                )
                ).togetherWith(
                fadeOut(
                    animationSpec = tween(durationMillis = 150),
                ) + scaleOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    targetScale = 0.8f,
                ),
            )
        },
        label = "recipientSelectionContactAvatar",
    ) { isSelectedState ->
        ParticipantAvatar(
            avatarUri = recipientSelectionPhotoUri(item = item),
            size = RECIPIENT_SELECTION_AVATAR_SIZE,
            fallbackLabel = participantAvatarLabel(source = displayName),
            modifier = Modifier.graphicsLayer {
                scaleX = avatarScale
                scaleY = avatarScale
            },
            colorSeedCode = participantColorSeed(
                normalizedDestination = recipientSelectionNormalizedDestination(item = item),
            ),
            isSelected = isSelectedState,
        )
    }
}

@Composable
internal fun recipientSelectionItemPrimaryText(
    item: RecipientPickerListItem,
): String {
    return when (item) {
        is RecipientPickerListItem.Contact -> item.contact.displayName
        is RecipientPickerListItem.SyntheticPhone -> {
            stringResource(
                id = R.string.contact_list_send_to_text,
                item.displayName,
            )
        }
    }
}

private fun recipientSelectionPhotoUri(item: RecipientPickerListItem): String? {
    return when (item) {
        is RecipientPickerListItem.Contact -> item.contact.photoUri
        is RecipientPickerListItem.SyntheticPhone -> null
    }
}

private fun recipientSelectionNormalizedDestination(item: RecipientPickerListItem): String? {
    return when (item) {
        is RecipientPickerListItem.Contact -> {
            item.contact.destinations.firstOrNull()?.normalizedValue
        }

        is RecipientPickerListItem.SyntheticPhone -> item.normalizedDestination
    }
}

@Composable
private fun rememberRecipientSelectionContactAvatarScale(
    isSelected: Boolean,
): State<Float> {
    val selectionTransition = updateTransition(
        targetState = isSelected,
        label = "recipientSelectionContactAvatarScale",
    )

    return selectionTransition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
        },
        label = "recipientSelectionContactAvatarScaleValue",
        targetValueByState = { isAvatarSelected ->
            when {
                isAvatarSelected -> 1f
                else -> 0.9f
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun RecipientSelectionContactAvatarPreview() {
    MessagingPreviewColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            previewRecipientPickerUiState().items.forEach { item ->
                RecipientSelectionContactAvatar(
                    item = item,
                    isSelected = false,
                )
                RecipientSelectionContactAvatar(
                    item = item,
                    isSelected = true,
                )
            }
        }
    }
}
