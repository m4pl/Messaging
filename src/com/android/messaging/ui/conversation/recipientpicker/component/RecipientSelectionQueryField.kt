@file:OptIn(ExperimentalComposeUiApi::class)

package com.android.messaging.ui.conversation.recipientpicker.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionQueryFieldLayout
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionQueryFieldUiState

@Composable
internal fun RecipientSelectionQueryField(
    uiState: RecipientSelectionQueryFieldUiState,
    fieldValue: TextFieldValue,
    onFieldValueChanged: (TextFieldValue) -> Unit,
    onQueryChanged: (String) -> Unit,
    onQueryFocusChanged: (Boolean) -> Unit,
    onLastSelectedRecipientRemove: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    layout: RecipientSelectionQueryFieldLayout = RecipientSelectionQueryFieldLayout.FULL_WIDTH,
    maxInlineWidth: Dp? = null,
) {
    BasicTextField(
        modifier = modifier
            .recipientSelectionQueryFieldModifier(
                uiState = uiState,
                layout = layout,
                maxInlineWidth = maxInlineWidth,
            )
            .focusRequester(focusRequester = focusRequester)
            .onFocusChanged { focusState ->
                onQueryFocusChanged(focusState.isFocused)
            }
            .onPreviewKeyEvent { keyEvent ->
                val shouldRemoveLastRecipient = shouldRemoveLastRecipientFromHardwareBackspace(
                    keyEvent = keyEvent,
                    fieldValue = fieldValue,
                    uiState = uiState,
                )

                if (shouldRemoveLastRecipient) {
                    onLastSelectedRecipientRemove()
                }

                shouldRemoveLastRecipient
            },
        value = fieldValue,
        onValueChange = { nextFieldValue ->
            handleRecipientSelectionQueryFieldValueChange(
                nextFieldValue = nextFieldValue,
                fieldValue = fieldValue,
                uiState = uiState,
                onFieldValueChanged = onFieldValueChanged,
                onQueryChanged = onQueryChanged,
                onLastSelectedRecipientRemove = onLastSelectedRecipientRemove,
            )
        },
        enabled = uiState.enabled,
        singleLine = true,
        textStyle = recipientSelectionQueryTextStyle(uiState = uiState),
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        visualTransformation = RecipientSelectionHiddenBackspaceTargetVisualTransformation,
        decorationBox = { innerTextField ->
            RecipientSelectionQueryFieldDecoration(
                uiState = uiState,
                layout = layout,
                innerTextField = innerTextField,
            )
        },
    )
}

private fun handleRecipientSelectionQueryFieldValueChange(
    nextFieldValue: TextFieldValue,
    fieldValue: TextFieldValue,
    uiState: RecipientSelectionQueryFieldUiState,
    onFieldValueChanged: (TextFieldValue) -> Unit,
    onQueryChanged: (String) -> Unit,
    onLastSelectedRecipientRemove: () -> Unit,
) {
    val shouldRemoveLastRecipient = shouldRemoveLastRecipientAfterHiddenBackspaceTargetDeleted(
        previousText = fieldValue.text,
        nextText = nextFieldValue.text,
        uiState = uiState,
    )

    when {
        shouldRemoveLastRecipient -> {
            onLastSelectedRecipientRemove()
        }

        else -> {
            val nextQuery = recipientSelectionVisibleQueryText(nextFieldValue.text)
            val nextVisibleFieldValue = nextFieldValue.withoutHiddenBackspaceTarget()

            onFieldValueChanged(nextVisibleFieldValue)

            if (nextQuery != uiState.query) {
                onQueryChanged(nextQuery)
            }
        }
    }
}

@Composable
private fun RecipientSelectionQueryFieldDecoration(
    uiState: RecipientSelectionQueryFieldUiState,
    layout: RecipientSelectionQueryFieldLayout,
    innerTextField: @Composable () -> Unit,
) {
    when (layout) {
        RecipientSelectionQueryFieldLayout.FULL_WIDTH -> {
            RecipientSelectionFullWidthQueryFieldDecoration(
                uiState = uiState,
                innerTextField = innerTextField,
            )
        }

        RecipientSelectionQueryFieldLayout.INLINE -> {
            RecipientSelectionInlineQueryFieldDecoration(
                uiState = uiState,
                innerTextField = innerTextField,
            )
        }
    }
}

@Composable
private fun RecipientSelectionFullWidthQueryFieldDecoration(
    uiState: RecipientSelectionQueryFieldUiState,
    innerTextField: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues = recipientSelectionQueryFieldPadding(uiState = uiState)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (uiState.prefixText.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(end = 12.dp),
                text = uiState.prefixText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box(modifier = Modifier.weight(weight = 1f)) {
            if (uiState.query.isEmpty()) {
                Text(
                    text = uiState.placeholderText,
                    style = recipientSelectionQueryPlaceholderTextStyle(uiState = uiState),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            innerTextField()
        }
    }
}

@Composable
private fun RecipientSelectionInlineQueryFieldDecoration(
    uiState: RecipientSelectionQueryFieldUiState,
    innerTextField: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp),
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
}

@Composable
private fun Modifier.recipientSelectionQueryFieldModifier(
    uiState: RecipientSelectionQueryFieldUiState,
    layout: RecipientSelectionQueryFieldLayout,
    maxInlineWidth: Dp?,
): Modifier {
    return when (layout) {
        RecipientSelectionQueryFieldLayout.FULL_WIDTH -> fillMaxWidth()

        RecipientSelectionQueryFieldLayout.INLINE -> {
            width(
                width = recipientSelectionInlineQueryFieldWidth(
                    uiState = uiState,
                    maxInlineWidth = maxInlineWidth,
                ),
            )
        }
    }
}

@Composable
private fun recipientSelectionInlineQueryFieldWidth(
    uiState: RecipientSelectionQueryFieldUiState,
    maxInlineWidth: Dp?,
): Dp {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textStyle = recipientSelectionQueryTextStyle(uiState = uiState)
    val textForMeasurement = recipientSelectionInlineQueryFieldWidthText(uiState = uiState)
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

    val desiredWidth = maxOf(
        48.dp,
        measuredTextWidth + 4.dp,
    )

    return when {
        maxInlineWidth == null -> desiredWidth
        else -> minOf(desiredWidth, maxInlineWidth)
    }
}

private fun recipientSelectionInlineQueryFieldWidthText(
    uiState: RecipientSelectionQueryFieldUiState,
): String {
    return when {
        uiState.query.isEmpty() -> uiState.placeholderText
        else -> uiState.query
    }
}

private fun recipientSelectionQueryFieldPadding(
    uiState: RecipientSelectionQueryFieldUiState,
): PaddingValues {
    return when {
        uiState.prefixText.isEmpty() -> {
            PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 12.dp)
        }

        else -> {
            PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        }
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
        uiState.prefixText.isEmpty() -> {
            MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal)
        }

        else -> MaterialTheme.typography.bodyLarge
    }
}
