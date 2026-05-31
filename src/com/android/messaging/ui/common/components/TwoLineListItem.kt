package com.android.messaging.ui.common.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val ItemHorizontalPadding = 8.dp
private val ItemVerticalPadding = 8.dp
private val ListRowShape = RoundedCornerShape(percent = 50)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TwoLineListItem(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    leadingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    shape: Shape = ListRowShape,
    color: Color = MaterialTheme.colorScheme.background,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = color,
    ) {
        val clickModifier = when (onLongClick) {
            null -> Modifier.clickable(onClick = onClick)
            else -> Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
        }

        Row(
            modifier = clickModifier.padding(
                horizontal = ItemHorizontalPadding,
                vertical = ItemVerticalPadding,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent()

            Spacer(modifier = Modifier.width(ItemHorizontalPadding))

            TwoLineListItemText(
                title = title,
                subtitle = subtitle,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = ItemHorizontalPadding),
            )

            trailingContent?.invoke()
        }
    }
}

@Composable
private fun TwoLineListItemText(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    val twoLineHeight = with(LocalDensity.current) {
        MaterialTheme.typography.bodyLarge.lineHeight.toDp() +
            MaterialTheme.typography.bodySmall.lineHeight.toDp()
    }

    Column(
        modifier = modifier.heightIn(min = twoLineHeight),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
