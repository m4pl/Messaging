@file:OptIn(ExperimentalComposeUiApi::class)

package com.android.messaging.ui.conversation.recipientpicker.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.messaging.ui.conversation.RECIPIENT_SELECTION_QUERY_FIELD_TEST_TAG
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionQueryFieldUiState

@Composable
internal fun RecipientSelectionQueryField(
    uiState: RecipientSelectionQueryFieldUiState,
    state: TextFieldState,
    onQueryFocusChanged: (Boolean) -> Unit,
    onLastSelectedRecipientRemove: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    maxWidth: Dp? = null,
) {
    BasicTextField(
        modifier = modifier
            .testTag(tag = RECIPIENT_SELECTION_QUERY_FIELD_TEST_TAG)
            .width(
                width = recipientSelectionQueryFieldWidth(
                    uiState = uiState,
                    maxWidth = maxWidth,
                ),
            )
            .focusRequester(focusRequester = focusRequester)
            .onFocusChanged { focusState ->
                onQueryFocusChanged(focusState.isFocused)
            }
            .onPreviewKeyEvent { keyEvent ->
                val shouldRemoveLastRecipient = shouldRemoveLastRecipientFromHardwareBackspace(
                    keyEvent = keyEvent,
                    text = state.text,
                    selection = state.selection,
                    uiState = uiState,
                )

                if (shouldRemoveLastRecipient) {
                    onLastSelectedRecipientRemove()
                }

                shouldRemoveLastRecipient
            },
        state = state,
        enabled = uiState.enabled,
        lineLimits = TextFieldLineLimits.SingleLine,
        textStyle = recipientSelectionQueryTextStyle(uiState = uiState),
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        inputTransformation = RecipientSelectionHiddenBackspaceTargetInputTransformation,
        outputTransformation = RecipientSelectionHiddenBackspaceTargetOutputTransformation,
        decorator = TextFieldDecorator { innerTextField ->
            Box(
                modifier = Modifier.heightIn(min = 32.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (uiState.query.isEmpty()) {
                    Text(
                        text = uiState.placeholderText,
                        style = recipientSelectionQueryPlaceholderTextStyle(uiState = uiState),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                innerTextField()
            }
        },
    )
}

@Composable
private fun recipientSelectionQueryFieldWidth(
    uiState: RecipientSelectionQueryFieldUiState,
    maxWidth: Dp?,
): Dp {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textStyle = recipientSelectionQueryTextStyle(uiState = uiState)
    val textForMeasurement = recipientSelectionQueryFieldWidthText(uiState = uiState)
    val measuredTextWidth = remember(
        textMeasurer,
        density,
        textStyle,
        textForMeasurement,
    ) {
        with(density) {
            textMeasurer
                .measure(
                    text = AnnotatedString(text = textForMeasurement),
                    style = textStyle,
                    maxLines = 1,
                )
                .size
                .width
                .toDp()
        }
    }

    val desiredWidth = maxOf(48.dp, measuredTextWidth + 4.dp)

    return when {
        maxWidth == null -> desiredWidth
        else -> minOf(desiredWidth, maxWidth)
    }
}

private fun recipientSelectionQueryFieldWidthText(
    uiState: RecipientSelectionQueryFieldUiState,
): String {
    return when {
        uiState.query.isEmpty() -> uiState.placeholderText
        else -> uiState.query
    }
}

@Composable
private fun recipientSelectionQueryTextStyle(
    uiState: RecipientSelectionQueryFieldUiState,
): TextStyle {
    return recipientSelectionQueryPlaceholderTextStyle(uiState = uiState).copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun recipientSelectionQueryPlaceholderTextStyle(
    uiState: RecipientSelectionQueryFieldUiState,
): TextStyle {
    return when {
        uiState.selectedRecipients.isEmpty() -> MaterialTheme.typography.bodyLarge
        else -> {
            MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal)
        }
    }
}
