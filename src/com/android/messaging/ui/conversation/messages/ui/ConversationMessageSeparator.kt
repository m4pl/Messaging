package com.android.messaging.ui.conversation.messages.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val messagesSeparatorSpacing = 12.dp
private val messagesSeparatorPadding = PaddingValues(
    horizontal = 14.dp,
    vertical = 6.dp,
)

@Composable
internal fun ColumnWithSeparator(
    modifier: Modifier,
    showDateSeparator: Boolean,
    dateSeparatorText: String?,
    content: @Composable () -> Unit,
) {
    val verticalSpace = when {
        showDateSeparator -> messagesSeparatorSpacing
        else -> 0.dp
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = verticalSpace),
    ) {
        if (showDateSeparator && dateSeparatorText != null) {
            ConversationDateSeparator(
                text = dateSeparatorText,
            )
        }

        content()
    }
}

@Composable
private fun ConversationDateSeparator(
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(messagesSeparatorPadding),
        )
    }
}
