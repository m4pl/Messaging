package com.android.messaging.ui.conversation.messages.ui.message

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.android.messaging.R
import com.android.messaging.ui.conversation.messages.model.message.ConversationMessageUiModel
import com.android.messaging.ui.conversation.messages.model.message.ConversationMessageUiModel.Status

private const val METADATA_SEPARATOR = " • "
private const val SIM_ANNOTATION_PLACEHOLDER = "%1\$s"

@Composable
internal fun ConversationMessageMetadata(
    message: ConversationMessageUiModel,
    metadataText: String?,
    simDisplayName: String?,
    onSimSelectorClick: () -> Unit,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val resources = LocalResources.current

    val annotatedText = remember(
        metadataText,
        simDisplayName,
        linkColor,
        resources,
        onSimSelectorClick,
    ) {
        buildMessageMetadataAnnotatedString(
            metadataText = metadataText,
            simDisplayName = simDisplayName,
            simAnnotationTemplate = resources.getString(
                R.string.conversation_message_sim_annotation,
            ),
            linkColor = linkColor,
            onSimSelectorClick = onSimSelectorClick,
        )
    }

    if (annotatedText == null) {
        return
    }

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        text = annotatedText,
        style = MaterialTheme.typography.labelSmall,
        color = messageMetadataColor(message = message),
        textAlign = when {
            message.isIncoming -> TextAlign.Start
            else -> TextAlign.End
        },
    )
}

private fun buildMessageMetadataAnnotatedString(
    metadataText: String?,
    simDisplayName: String?,
    simAnnotationTemplate: String,
    linkColor: Color,
    onSimSelectorClick: () -> Unit,
): AnnotatedString? {
    return when {
        metadataText == null && simDisplayName == null -> null
        simDisplayName == null -> AnnotatedString(text = metadataText.orEmpty())
        else -> buildSimLinkAnnotatedString(
            metadataText = metadataText,
            simDisplayName = simDisplayName,
            simAnnotationTemplate = simAnnotationTemplate,
            linkColor = linkColor,
            onSimSelectorClick = onSimSelectorClick,
        )
    }
}

private fun buildSimLinkAnnotatedString(
    metadataText: String?,
    simDisplayName: String,
    simAnnotationTemplate: String,
    linkColor: Color,
    onSimSelectorClick: () -> Unit,
): AnnotatedString {
    val placeholderIndex = simAnnotationTemplate.indexOf(SIM_ANNOTATION_PLACEHOLDER)

    val annotationPrefix = when {
        placeholderIndex >= 0 -> simAnnotationTemplate.substring(0, placeholderIndex)
        else -> simAnnotationTemplate
    }

    val annotationSuffix = when {
        placeholderIndex >= 0 -> {
            simAnnotationTemplate.substring(
                placeholderIndex + SIM_ANNOTATION_PLACEHOLDER.length,
            )
        }
        else -> ""
    }

    val link = LinkAnnotation.Clickable(
        tag = SIM_LINK_TAG,
        styles = TextLinkStyles(
            style = SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline,
            ),
        ),
    ) {
        onSimSelectorClick()
    }

    return buildAnnotatedString {
        if (!metadataText.isNullOrEmpty()) {
            append(metadataText)
            append(METADATA_SEPARATOR)
        }

        append(annotationPrefix)

        withLink(link = link) {
            append(simDisplayName)
        }

        if (annotationSuffix.isNotEmpty()) {
            append(annotationSuffix)
        }
    }
}

@Composable
private fun messageMetadataColor(
    message: ConversationMessageUiModel,
): Color {
    return when (message.status) {
        Status.Outgoing.AwaitingRetry,
        Status.Outgoing.Failed,
        Status.Outgoing.FailedEmergencyNumber,
        Status.Incoming.DownloadFailed,
        Status.Incoming.ExpiredOrNotAvailable,
        -> MaterialTheme.colorScheme.error

        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private const val SIM_LINK_TAG = "sim_selector"
