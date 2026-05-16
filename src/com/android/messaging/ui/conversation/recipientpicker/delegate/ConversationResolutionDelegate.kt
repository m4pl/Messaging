package com.android.messaging.ui.conversation.recipientpicker.delegate

import com.android.messaging.di.core.MainDispatcher
import com.android.messaging.domain.conversation.usecase.participant.ResolveConversationId
import com.android.messaging.domain.conversation.usecase.participant.model.ResolveConversationIdResult
import com.android.messaging.ui.conversation.recipientpicker.model.picker.ConversationResolutionOutcome
import com.android.messaging.ui.conversation.recipientpicker.model.picker.ConversationResolutionState
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface ConversationResolutionDelegate {
    val state: StateFlow<ConversationResolutionState>
    val outcomes: Flow<ConversationResolutionOutcome>

    fun bind(scope: CoroutineScope)

    fun resolve(
        destinations: List<String>,
        recipientDestination: String? = null,
    )

    fun cancel()
}

internal class ConversationResolutionDelegateImpl @Inject constructor(
    private val resolveConversationId: ResolveConversationId,
    @param:MainDispatcher
    private val mainDispatcher: CoroutineDispatcher,
) : ConversationResolutionDelegate {

    private val _state = MutableStateFlow<ConversationResolutionState>(
        value = ConversationResolutionState.Idle,
    )
    override val state = _state.asStateFlow()

    private val outcomesChannel = Channel<ConversationResolutionOutcome>(
        capacity = Channel.BUFFERED,
    )
    override val outcomes = outcomesChannel.receiveAsFlow()

    private var boundScope: CoroutineScope? = null
    private var resolveJob: Job? = null

    override fun bind(scope: CoroutineScope) {
        if (boundScope != null) {
            return
        }

        boundScope = scope
    }

    override fun resolve(
        destinations: List<String>,
        recipientDestination: String?,
    ) {
        val scope = boundScope ?: return
        cancelInFlightResolve()

        resolveJob = scope.launch(mainDispatcher) {
            _state.value = ConversationResolutionState.Resolving(
                recipientDestination = recipientDestination,
                isIndicatorVisible = false,
            )

            val indicatorJob = launch(mainDispatcher) {
                delay(INDICATOR_DELAY)
                _state.update { current ->
                    when (current) {
                        is ConversationResolutionState.Resolving -> {
                            current.copy(isIndicatorVisible = true)
                        }

                        ConversationResolutionState.Idle -> current
                    }
                }
            }

            try {
                val outcome = mapResultToOutcome(
                    result = resolveConversationId(destinations = destinations),
                )
                outcomesChannel.send(element = outcome)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                outcomesChannel.send(element = ConversationResolutionOutcome.Failed)
            } finally {
                indicatorJob.cancel()

                if (resolveJob === coroutineContext[Job]) {
                    resolveJob = null
                    _state.value = ConversationResolutionState.Idle
                }
            }
        }
    }

    override fun cancel() {
        cancelInFlightResolve()
    }

    private fun cancelInFlightResolve() {
        val currentJob = resolveJob
        resolveJob = null
        currentJob?.cancel()
        _state.value = ConversationResolutionState.Idle
    }

    private fun mapResultToOutcome(
        result: ResolveConversationIdResult,
    ): ConversationResolutionOutcome {
        return when (result) {
            is ResolveConversationIdResult.Resolved -> {
                ConversationResolutionOutcome.Resolved(
                    conversationId = result.conversationId,
                )
            }

            ResolveConversationIdResult.EmptyDestinations,
            ResolveConversationIdResult.NotResolved,
            -> ConversationResolutionOutcome.Failed
        }
    }

    private companion object {
        private val INDICATOR_DELAY = 200L.milliseconds
    }
}
