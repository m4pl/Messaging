package com.android.messaging.ui.conversationpicker.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.messaging.ui.common.components.composer.MessageSubjectChip

internal fun composeSubjectSlot(
    subjectText: String,
    onClear: () -> Unit,
): (@Composable ColumnScope.() -> Unit)? {
    if (subjectText.isBlank()) {
        return null
    }

    return {
        MessageSubjectChip(
            modifier = Modifier.testTag(tag = SUBJECT_CHIP_TEST_TAG),
            subjectText = subjectText,
            onClick = null,
            onClear = onClear,
            clearButtonTestTag = SUBJECT_CHIP_CLEAR_BUTTON_TEST_TAG,
        )
    }
}
