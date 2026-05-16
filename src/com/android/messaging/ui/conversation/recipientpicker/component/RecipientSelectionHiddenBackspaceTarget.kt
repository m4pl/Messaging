package com.android.messaging.ui.conversation.recipientpicker.component

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionQueryFieldUiState

// Some soft keyboards do not emit key events for Backspace on a visually empty field
private const val RECIPIENT_SELECTION_BACKSPACE_SENTINEL = "\u2060"

internal object RecipientSelectionHiddenBackspaceTargetVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val visibleText = recipientSelectionVisibleQueryText(fieldText = text.text)

        return when {
            visibleText == text.text -> {
                TransformedText(
                    text = text,
                    offsetMapping = OffsetMapping.Identity,
                )
            }

            else -> {
                TransformedText(
                    text = AnnotatedString(text = visibleText),
                    offsetMapping = RecipientSelectionHiddenBackspaceTargetOffsetMapping,
                )
            }
        }
    }
}

private object RecipientSelectionHiddenBackspaceTargetOffsetMapping : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        return offset.withoutHiddenBackspaceTargetOffset()
    }

    override fun transformedToOriginal(offset: Int): Int {
        return offset + RECIPIENT_SELECTION_BACKSPACE_SENTINEL.length
    }
}

internal fun recipientSelectionVisibleQueryText(fieldText: String): String {
    return fieldText.removePrefix(RECIPIENT_SELECTION_BACKSPACE_SENTINEL)
}

internal fun TextFieldValue.withoutHiddenBackspaceTarget(): TextFieldValue {
    val nextText = recipientSelectionVisibleQueryText(fieldText = text)

    return when {
        nextText == text -> this

        else -> {
            copy(
                text = nextText,
                selection = selection.withoutHiddenBackspaceTarget(),
                composition = composition?.withoutHiddenBackspaceTarget(),
            )
        }
    }
}

private fun TextRange.withoutHiddenBackspaceTarget(): TextRange {
    return TextRange(
        start = start.withoutHiddenBackspaceTargetOffset(),
        end = end.withoutHiddenBackspaceTargetOffset(),
    )
}

private fun Int.withoutHiddenBackspaceTargetOffset(): Int {
    return when {
        this <= RECIPIENT_SELECTION_BACKSPACE_SENTINEL.length -> 0
        else -> this - RECIPIENT_SELECTION_BACKSPACE_SENTINEL.length
    }
}

internal fun recipientSelectionQueryFieldEditableText(
    uiState: RecipientSelectionQueryFieldUiState,
): String {
    return when {
        shouldUseHiddenBackspaceTarget(uiState = uiState) -> {
            RECIPIENT_SELECTION_BACKSPACE_SENTINEL
        }

        else -> uiState.query
    }
}

internal fun recipientSelectionTextFieldValue(text: String): TextFieldValue {
    return TextFieldValue(
        text = text,
        selection = TextRange(index = text.length),
    )
}

private fun shouldUseHiddenBackspaceTarget(
    uiState: RecipientSelectionQueryFieldUiState,
): Boolean {
    return uiState.query.isEmpty() && uiState.selectedRecipients.isNotEmpty()
}

internal fun shouldRemoveLastRecipientFromHardwareBackspace(
    keyEvent: KeyEvent,
    fieldValue: TextFieldValue,
    uiState: RecipientSelectionQueryFieldUiState,
): Boolean {
    return keyEvent.key == Key.Backspace &&
        keyEvent.type == KeyEventType.KeyDown &&
        shouldRemoveLastRecipientFromVisibleEmptyQueryBackspace(
            fieldValue = fieldValue,
            uiState = uiState,
        )
}

private fun shouldRemoveLastRecipientFromVisibleEmptyQueryBackspace(
    fieldValue: TextFieldValue,
    uiState: RecipientSelectionQueryFieldUiState,
): Boolean {
    val visibleFieldValue = fieldValue.withoutHiddenBackspaceTarget()

    return uiState.enabled &&
        shouldUseHiddenBackspaceTarget(uiState = uiState) &&
        visibleFieldValue.text.isEmpty() &&
        visibleFieldValue.selection.start == visibleFieldValue.selection.end &&
        visibleFieldValue.selection.start == 0
}

internal fun shouldRemoveLastRecipientAfterHiddenBackspaceTargetDeleted(
    previousText: String,
    nextText: String,
    uiState: RecipientSelectionQueryFieldUiState,
): Boolean {
    return previousText == RECIPIENT_SELECTION_BACKSPACE_SENTINEL &&
        nextText.isEmpty() &&
        shouldUseHiddenBackspaceTarget(uiState = uiState)
}
