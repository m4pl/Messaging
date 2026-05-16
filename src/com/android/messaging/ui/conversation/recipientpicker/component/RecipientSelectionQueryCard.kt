package com.android.messaging.ui.conversation.recipientpicker.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.android.messaging.ui.conversation.recipientpicker.model.picker.SelectedRecipient
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionContentUiState
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionQueryCardUiState
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionQueryChipsUiState
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionQueryFieldLayout
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionQueryFieldPlacement
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionQueryFieldUiState
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionQueryTextUiState
import com.android.messaging.ui.conversation.recipientpicker.model.selection.RecipientSelectionStrings
import kotlinx.collections.immutable.ImmutableList

private val searchCardShape = RoundedCornerShape(size = 22.dp)

internal fun recipientSelectionQueryCardUiState(
    uiState: RecipientSelectionContentUiState,
    strings: RecipientSelectionStrings,
    armedRecipientDestination: String?,
): RecipientSelectionQueryCardUiState {
    return RecipientSelectionQueryCardUiState(
        text = RecipientSelectionQueryTextUiState(
            query = uiState.picker.query,
            enabled = uiState.isQueryEnabled,
            prefixText = strings.queryPrefixText,
            placeholderText = strings.queryPlaceholderText,
        ),
        chips = RecipientSelectionQueryChipsUiState(
            recipients = uiState.selectedRecipients,
            armedRecipientDestination = armedRecipientDestination,
            enabled = recipientSelectionMutationsEnabled(uiState = uiState),
        ),
    )
}

@Composable
internal fun RecipientSelectionQueryCard(
    uiState: RecipientSelectionQueryCardUiState,
    onQueryChanged: (String) -> Unit,
    onQueryFocused: () -> Unit,
    onSelectedRecipientClick: (SelectedRecipient) -> Unit,
    onSelectedRecipientBackspace: (SelectedRecipient) -> Unit,
    focusRequester: FocusRequester,
    simSelectorSlot: (@Composable () -> Unit)?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = searchCardShape,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            RecipientSelectionQueryCardBody(
                uiState = uiState,
                onQueryChanged = onQueryChanged,
                onQueryFocused = onQueryFocused,
                onSelectedRecipientClick = onSelectedRecipientClick,
                onSelectedRecipientBackspace = onSelectedRecipientBackspace,
                focusRequester = focusRequester,
            )
            simSelectorSlot?.invoke()
        }
    }
}

@Composable
private fun RecipientSelectionQueryCardBody(
    uiState: RecipientSelectionQueryCardUiState,
    onQueryChanged: (String) -> Unit,
    onQueryFocused: () -> Unit,
    onSelectedRecipientClick: (SelectedRecipient) -> Unit,
    onSelectedRecipientBackspace: (SelectedRecipient) -> Unit,
    focusRequester: FocusRequester,
) {
    val queryFieldUiState = queryFieldUiState(uiState = uiState)
    val editableText = recipientSelectionQueryFieldEditableText(uiState = queryFieldUiState)
    var queryFieldValue by remember {
        mutableStateOf(value = recipientSelectionTextFieldValue(text = editableText))
    }

    LaunchedEffect(editableText) {
        if (queryFieldValue.text != editableText) {
            queryFieldValue = recipientSelectionTextFieldValue(text = editableText)
        }
    }

    val queryFieldContent = rememberRecipientSelectionQueryFieldContent(
        focusRequester = focusRequester,
        onQueryFieldValueChanged = { queryFieldValue = it },
        onQueryChanged = onQueryChanged,
        onQueryFocused = onQueryFocused,
        onLastSelectedRecipientRemove = {
            uiState.chips.recipients.lastOrNull()?.let { recipient ->
                onSelectedRecipientBackspace(recipient)
            }
        },
    )

    when {
        uiState.chips.recipients.isNotEmpty() -> {
            RecipientSelectionSelectedRecipientsHeader(
                prefixText = uiState.text.prefixText,
                recipients = uiState.chips.recipients,
                armedRecipientDestination = uiState.chips.armedRecipientDestination,
                enabled = uiState.chips.enabled,
                queryFieldUiState = queryFieldUiState,
                queryFieldValue = queryFieldValue,
                onQueryFocusChanged = { isFocused ->
                    if (isFocused) onQueryFocused()
                },
                onRecipientClick = onSelectedRecipientClick,
                focusRequester = focusRequester,
                queryFieldContent = queryFieldContent,
            )
        }

        else -> {
            queryFieldContent(
                RecipientSelectionQueryFieldPlacement(),
                queryFieldUiState,
                queryFieldValue,
            )
        }
    }
}

@Composable
private fun rememberRecipientSelectionQueryFieldContent(
    focusRequester: FocusRequester,
    onQueryFieldValueChanged: (TextFieldValue) -> Unit,
    onQueryChanged: (String) -> Unit,
    onQueryFocused: () -> Unit,
    onLastSelectedRecipientRemove: () -> Unit,
): @Composable (
    RecipientSelectionQueryFieldPlacement,
    RecipientSelectionQueryFieldUiState,
    TextFieldValue,
) -> Unit {
    val currentOnQueryFieldValueChanged = rememberUpdatedState(newValue = onQueryFieldValueChanged)
    val currentOnQueryChanged = rememberUpdatedState(newValue = onQueryChanged)
    val currentOnQueryFocused = rememberUpdatedState(newValue = onQueryFocused)
    val currentOnLastSelectedRecipientRemove = rememberUpdatedState(
        newValue = onLastSelectedRecipientRemove,
    )

    return remember(focusRequester) {
        movableContentOf { placement, contentUiState, contentFieldValue ->
            RecipientSelectionQueryField(
                modifier = placement.modifier,
                uiState = contentUiState,
                fieldValue = contentFieldValue,
                onFieldValueChanged = { value -> currentOnQueryFieldValueChanged.value(value) },
                onQueryChanged = { query -> currentOnQueryChanged.value(query) },
                onQueryFocusChanged = { isFocused ->
                    if (isFocused) currentOnQueryFocused.value()
                },
                onLastSelectedRecipientRemove = { currentOnLastSelectedRecipientRemove.value() },
                focusRequester = focusRequester,
                layout = placement.layout,
                maxInlineWidth = placement.maxInlineWidth,
            )
        }
    }
}

@Composable
private fun RecipientSelectionSelectedRecipientsHeader(
    prefixText: String,
    recipients: ImmutableList<SelectedRecipient>,
    armedRecipientDestination: String?,
    enabled: Boolean,
    queryFieldUiState: RecipientSelectionQueryFieldUiState,
    queryFieldValue: TextFieldValue,
    onQueryFocusChanged: (Boolean) -> Unit,
    onRecipientClick: (SelectedRecipient) -> Unit,
    focusRequester: FocusRequester,
    queryFieldContent: @Composable (
        RecipientSelectionQueryFieldPlacement,
        RecipientSelectionQueryFieldUiState,
        TextFieldValue,
    ) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val currentOnQueryFocusChanged = rememberUpdatedState(newValue = onQueryFocusChanged)
    val currentKeyboardController = rememberUpdatedState(newValue = keyboardController)
    val onInputAreaTap: () -> Unit = remember(focusRequester) {
        {
            currentOnQueryFocusChanged.value(true)
            focusRequester.requestFocus()
            currentKeyboardController.value?.show()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .recipientSelectionFocusQueryOnUnhandledTap(
                enabled = queryFieldUiState.enabled,
                onTap = onInputAreaTap,
            )
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
    ) {
        RecipientSelectionSelectedRecipientChips(
            recipients = recipients,
            armedRecipientDestination = armedRecipientDestination,
            enabled = enabled,
            onRecipientClick = onRecipientClick,
            leadingContent = {
                Text(
                    modifier = Modifier.padding(top = 8.dp, end = 4.dp),
                    text = prefixText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingContent = { modifier ->
                queryFieldContent(
                    RecipientSelectionQueryFieldPlacement(
                        modifier = modifier,
                        layout = RecipientSelectionQueryFieldLayout.INLINE,
                        maxInlineWidth = maxWidth,
                    ),
                    queryFieldUiState,
                    queryFieldValue,
                )
            },
        )
    }
}

private fun Modifier.recipientSelectionFocusQueryOnUnhandledTap(
    enabled: Boolean,
    onTap: () -> Unit,
): Modifier {
    return when {
        !enabled -> this

        else -> {
            pointerInput(key1 = enabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Final,
                    )

                    if (down.isConsumed) {
                        return@awaitEachGesture
                    }

                    val up = waitForUpOrCancellation(pass = PointerEventPass.Final)

                    if (up != null && !up.isConsumed) {
                        onTap()
                    }
                }
            }
        }
    }
}

private fun queryFieldUiState(
    uiState: RecipientSelectionQueryCardUiState,
): RecipientSelectionQueryFieldUiState {
    return RecipientSelectionQueryFieldUiState(
        query = uiState.text.query,
        enabled = uiState.text.enabled,
        prefixText = when {
            uiState.chips.recipients.isEmpty() -> uiState.text.prefixText
            else -> ""
        },
        placeholderText = uiState.text.placeholderText,
        selectedRecipients = uiState.chips.recipients,
    )
}

private fun recipientSelectionMutationsEnabled(
    uiState: RecipientSelectionContentUiState,
): Boolean {
    return uiState.isQueryEnabled && uiState.primaryAction?.isLoading != true
}
