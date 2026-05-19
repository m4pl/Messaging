package com.android.messaging.ui.conversationsettings.screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.android.messaging.R
import com.android.messaging.ui.conversationsettings.screen.model.ConversationSettingsAction
import com.android.messaging.ui.conversationsettings.screen.support.ConversationSettingsTestBase
import com.android.messaging.ui.conversationsettings.screen.support.FATHER_DESTINATION
import com.android.messaging.ui.conversationsettings.screen.support.FATHER_NAME
import com.android.messaging.ui.conversationsettings.screen.support.MOTHER_DESTINATION
import com.android.messaging.ui.conversationsettings.screen.support.MOTHER_NAME
import com.android.messaging.ui.conversationsettings.screen.support.groupState
import com.android.messaging.ui.conversationsettings.screen.support.oneToOneState
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

internal class ParticipantsListTest : ConversationSettingsTestBase() {

    @Test
    fun displaysAllParticipants_inGroup() {
        renderScreen(groupState())

        composeTestRule.onNodeWithText(string(R.string.participant_list_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(MOTHER_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithText(FATHER_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithText(MOTHER_DESTINATION).assertIsDisplayed()
        composeTestRule.onNodeWithText(FATHER_DESTINATION).assertIsDisplayed()
    }

    @Test
    fun click_dispatchesParticipantPressed_withNormalizedDestination() {
        renderScreen(groupState())

        composeTestRule.onNodeWithText(MOTHER_NAME).performClick()

        verify(exactly = 1) {
            screenModel.onAction(
                ConversationSettingsAction.ParticipantPressed(MOTHER_DESTINATION),
            )
        }
    }

    @Test
    fun longPress_dispatchesParticipantLongPressed_withDetails() {
        renderScreen(groupState())

        composeTestRule.onNodeWithText(FATHER_NAME).performTouchInput { longClick() }

        verify(exactly = 1) {
            screenModel.onAction(
                ConversationSettingsAction.ParticipantLongPressed(FATHER_DESTINATION),
            )
        }
    }

    @Test
    fun infoButton_dispatchesParticipantActionPressed() {
        renderScreen(groupState())

        composeTestRule
            .onAllNodesWithContentDescription(string(R.string.action_contact_info))
            .filterToOne(hasAnyAncestor(hasText(MOTHER_NAME)))
            .performClick()

        verify(exactly = 1) {
            screenModel.onAction(
                ConversationSettingsAction.ParticipantActionPressed(MOTHER_DESTINATION),
            )
        }
    }

    @Test
    fun infoButton_hidden_forSingleParticipantConversation() {
        renderScreen(oneToOneState(canShowContact = false, canCall = false))

        composeTestRule
            .onAllNodesWithContentDescription(string(R.string.action_contact_info))
            .assertCountEquals(0)
    }

    @Test
    fun participantsSection_hidden_whenEmpty() {
        renderScreen(
            oneToOneState().copy(participants = persistentListOf()),
        )

        composeTestRule
            .onNodeWithText(string(R.string.participant_list_title))
            .assertDoesNotExist()
    }
}
