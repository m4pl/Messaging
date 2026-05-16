package com.android.messaging.ui.conversation.recipientpicker.component

internal fun recipientSelectionAvatarLabel(
    displayName: String,
    destination: String,
): String {
    val labelSource = displayName.ifBlank { destination }
    val firstCharacter = labelSource.firstOrNull() ?: '?'

    return firstCharacter.uppercaseChar().toString()
}
