package com.android.messaging.ui.conversation.messagedetails

import android.content.ClipData
import android.content.ClipboardManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.messaging.data.conversation.model.ConversationId
import com.android.messaging.data.conversation.model.MessageId
import com.android.messaging.data.conversation.repository.ConversationsRepository
import com.android.messaging.ui.conversation.messagedetails.mapper.MessageDetailsUiStateMapper
import com.android.messaging.ui.conversation.messagedetails.model.MessageDetailsUiState as State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal interface MessageDetailsScreenModel {
    val uiState: StateFlow<State>

    fun onCopy(value: String)
}

@HiltViewModel
internal class MessageDetailsViewModel @Inject constructor(
    private val conversationsRepository: ConversationsRepository,
    private val messageDetailsUiStateMapper: MessageDetailsUiStateMapper,
    private val clipboardManager: ClipboardManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel(),
    MessageDetailsScreenModel {

    private val _uiState = MutableStateFlow<State>(State.Loading)
    override val uiState = _uiState.asStateFlow()

    private val conversationId: ConversationId = requireNotNull(
        ConversationId.fromOrNull(savedStateHandle[CONVERSATION_ID_KEY]),
    ) { "conversationId is required" }

    private val messageId: MessageId = requireNotNull(
        MessageId.fromOrNull(savedStateHandle[MESSAGE_ID_KEY]),
    ) { "messageId is required" }

    init {
        bindMessageDetails()
    }

    private fun bindMessageDetails() {
        viewModelScope.launch {
            _uiState.value = loadMessageDetails()
        }
    }

    override fun onCopy(value: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, value))
    }

    private suspend fun loadMessageDetails(): State {
        val result = conversationsRepository.getMessageDetails(
            conversationId = conversationId,
            messageId = messageId,
        )

        return messageDetailsUiStateMapper.map(
            message = result?.message,
            details = result?.details,
        )
    }

    private companion object {
        private const val CONVERSATION_ID_KEY = "conversationId"
        private const val MESSAGE_ID_KEY = "messageId"
    }
}
