package com.android.messaging.ui.conversation.messages.ui.message

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.android.messaging.ui.conversation.messages.model.message.ConversationMessagePartUiModel
import com.android.messaging.ui.conversation.messages.model.message.ConversationMessageUiModel
import com.android.messaging.ui.core.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private const val MESSAGE_ID = "message-id"
private const val CONVERSATION_ID = "conversation-id"
private const val LINK_ONLY_TEXT = "https://example.com"
private const val PLAIN_TEXT = "plain outgoing message"
private const val TIMESTAMP = 1_700_000_000_000L

internal class ConversationMessageLinkLongClickTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longClickOutgoingLinkOnlyMessageSelectsMessage() {
        var externalUriClickCount = 0
        var messageLongClickCount = 0

        composeRule.setContent {
            AppTheme {
                ConversationMessage(
                    message = outgoingMessage(text = LINK_ONLY_TEXT),
                    onExternalUriClick = {
                        externalUriClickCount += 1
                    },
                    onMessageLongClick = {
                        messageLongClickCount += 1
                    },
                )
            }
        }

        composeRule.waitForIdle()

        composeRule
            .onNodeWithText(text = LINK_ONLY_TEXT, useUnmergedTree = true)
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, externalUriClickCount)
        }

        composeRule
            .onNodeWithText(text = LINK_ONLY_TEXT, useUnmergedTree = true)
            .performTouchInput {
                longClick(position = center)
            }

        composeRule.runOnIdle {
            assertEquals(1, externalUriClickCount)
            assertEquals(1, messageLongClickCount)
        }
    }

    @Test
    fun longClickOutgoingLinkOnlyMessageStaysSelectedAfterRelease() {
        var externalUriClickCount = 0
        var messageClickCount = 0
        var messageLongClickCount = 0
        var isSelected by mutableStateOf(false)
        var isSelectionMode by mutableStateOf(false)

        composeRule.setContent {
            AppTheme {
                ConversationMessage(
                    message = outgoingMessage(text = LINK_ONLY_TEXT),
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode,
                    onExternalUriClick = {
                        externalUriClickCount += 1
                    },
                    onMessageClick = {
                        messageClickCount += 1
                        isSelected = !isSelected
                    },
                    onMessageLongClick = {
                        messageLongClickCount += 1
                        isSelectionMode = true
                        isSelected = true
                    },
                )
            }
        }

        composeRule.waitForIdle()

        composeRule
            .onNodeWithText(text = LINK_ONLY_TEXT, useUnmergedTree = true)
            .performTouchInput {
                longClick(position = center)
            }

        composeRule.runOnIdle {
            assertEquals(0, externalUriClickCount)
            assertEquals(0, messageClickCount)
            assertEquals(1, messageLongClickCount)
            assertEquals(true, isSelected)
            assertEquals(true, isSelectionMode)
        }
    }

    @Test
    fun longClickOutgoingPlainTextMessageSelectsMessageOnce() {
        var messageLongClickCount = 0

        composeRule.setContent {
            AppTheme {
                ConversationMessage(
                    message = outgoingMessage(text = PLAIN_TEXT),
                    onMessageLongClick = {
                        messageLongClickCount += 1
                    },
                )
            }
        }

        composeRule.waitForIdle()

        composeRule
            .onNodeWithText(text = PLAIN_TEXT, useUnmergedTree = true)
            .performTouchInput {
                longClick(position = center)
            }

        composeRule.runOnIdle {
            assertEquals(1, messageLongClickCount)
        }
    }
}

private fun outgoingMessage(text: String): ConversationMessageUiModel {
    return ConversationMessageUiModel(
        messageId = MESSAGE_ID,
        conversationId = CONVERSATION_ID,
        text = text,
        parts = listOf(
            ConversationMessagePartUiModel.Text(
                text = text,
            ),
        ),
        sentTimestamp = TIMESTAMP,
        receivedTimestamp = TIMESTAMP,
        displayTimestamp = TIMESTAMP,
        status = ConversationMessageUiModel.Status.Outgoing.Complete,
        isIncoming = false,
        senderDisplayName = null,
        senderAvatarUri = null,
        senderContactLookupKey = null,
        selfParticipantId = null,
        canClusterWithPrevious = false,
        canClusterWithNext = false,
        canCopyMessageToClipboard = true,
        canDownloadMessage = false,
        canForwardMessage = true,
        canResendMessage = false,
        canSaveAttachments = false,
        mmsSubject = null,
        protocol = ConversationMessageUiModel.Protocol.SMS,
    )
}
