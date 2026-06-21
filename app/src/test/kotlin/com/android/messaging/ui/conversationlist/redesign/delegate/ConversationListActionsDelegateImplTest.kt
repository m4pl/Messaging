package com.android.messaging.ui.conversationlist.redesign.delegate

import com.android.messaging.data.blockedparticipants.repository.BlockedParticipantsRepository
import com.android.messaging.data.conversation.repository.ConversationsRepository
import com.android.messaging.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationListActionsDelegateImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun setPinned_pinsEachDistinctNonBlankConversation() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            val harness = createHarness()

            harness.delegate.setPinned(
                conversationIds = listOf("a", "", " ", "b", "a"),
                isPinned = true,
            )
            advanceUntilIdle()

            coVerify(exactly = 1) { harness.conversationsRepository.pinConversation("a") }
            coVerify(exactly = 1) { harness.conversationsRepository.pinConversation("b") }
            coVerify(exactly = 0) { harness.conversationsRepository.pinConversation("") }
            coVerify(exactly = 0) { harness.conversationsRepository.pinConversation(" ") }
        }
    }

    @Test
    fun setPinned_unpinsWhenNotPinned() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            val harness = createHarness()

            harness.delegate.setPinned(
                conversationIds = listOf("a"),
                isPinned = false,
            )
            advanceUntilIdle()

            coVerify(exactly = 1) { harness.conversationsRepository.unpinConversation("a") }
            coVerify(exactly = 0) { harness.conversationsRepository.pinConversation(any()) }
        }
    }

    @Test
    fun setPinned_emptyIds_doesNothing() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            val harness = createHarness()

            harness.delegate.setPinned(
                conversationIds = emptyList(),
                isPinned = true,
            )
            advanceUntilIdle()

            coVerify(exactly = 0) { harness.conversationsRepository.pinConversation(any()) }
            coVerify(exactly = 0) { harness.conversationsRepository.unpinConversation(any()) }
        }
    }

    @Test
    fun setPinned_blankOnlyIds_doesNothing() {
        runTest(context = mainDispatcherRule.testDispatcher) {
            val harness = createHarness()

            harness.delegate.setPinned(
                conversationIds = listOf("", "  "),
                isPinned = true,
            )
            advanceUntilIdle()

            coVerify(exactly = 0) { harness.conversationsRepository.pinConversation(any()) }
        }
    }

    private fun createHarness(): Harness {
        val conversationsRepository = mockk<ConversationsRepository>(relaxed = true)
        val delegate = ConversationListActionsDelegateImpl(
            conversationsRepository = conversationsRepository,
            blockedParticipantsRepository = mockk<BlockedParticipantsRepository>(relaxed = true),
        ).apply { bind(scope = TestScope(mainDispatcherRule.testDispatcher)) }

        return Harness(
            conversationsRepository = conversationsRepository,
            delegate = delegate,
        )
    }

    private class Harness(
        val conversationsRepository: ConversationsRepository,
        val delegate: ConversationListActionsDelegateImpl,
    )
}
