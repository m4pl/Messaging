package com.android.messaging.ui.conversation.messages.ui.message

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import com.android.messaging.datamodel.data.ParticipantData
import com.android.messaging.ui.conversation.messages.model.message.ConversationMessagePartUiModel
import com.android.messaging.ui.conversation.messages.model.message.ConversationMessageUiModel
import com.android.messaging.ui.core.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private const val MESSAGE_ID = "message-id"
private const val CONVERSATION_ID = "conversation-id"
private const val HEIGHT_ASSERTION_DELTA_DP = 0.5f
private const val LINK_ONLY_TEXT = "https://example.com"
private const val MESSAGE_TEST_TAG = "conversation-message"
private const val MINIMAL_FONT_SCALE = 0.85f
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

    @Test
    fun selectionModeDoesNotChangePlainTextMessageHeightAtSmallFontScale() {
        var isSelected by mutableStateOf(false)
        var isSelectionMode by mutableStateOf(false)

        composeRule.setContent {
            val density = LocalDensity.current

            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = density.density,
                    fontScale = MINIMAL_FONT_SCALE,
                ),
            ) {
                AppTheme {
                    ConversationMessage(
                        modifier = Modifier.testTag(tag = MESSAGE_TEST_TAG),
                        message = outgoingMessage(text = PLAIN_TEXT),
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                    )
                }
            }
        }

        composeRule.waitForIdle()

        val unselectedHeight = composeRule
            .onNodeWithTag(testTag = MESSAGE_TEST_TAG)
            .getUnclippedBoundsInRoot()
            .let { bounds ->
                bounds.bottom - bounds.top
            }

        composeRule.runOnIdle {
            isSelected = true
            isSelectionMode = true
        }
        composeRule.waitForIdle()

        val selectedHeight = composeRule
            .onNodeWithTag(testTag = MESSAGE_TEST_TAG)
            .getUnclippedBoundsInRoot()
            .let { bounds ->
                bounds.bottom - bounds.top
            }

        composeRule.runOnIdle {
            assertEquals(
                unselectedHeight.value,
                selectedHeight.value,
                HEIGHT_ASSERTION_DELTA_DP,
            )
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
        senderContactId = ParticipantData.PARTICIPANT_CONTACT_ID_NOT_RESOLVED,
        senderContactLookupKey = null,
        senderNormalizedDestination = null,
        senderParticipantId = null,
        selfParticipantId = null,
        canClusterWithPrevious = false,
        canClusterWithNext = false,
        canCopyMessageToClipboard = true,
        canDownloadMessage = false,
        canForwardMessage = true,
        canResendMessage = false,
        canSaveAttachments = false,
        mmsDownload = null,
        mmsSubject = null,
        protocol = ConversationMessageUiModel.Protocol.SMS,
    )
}
