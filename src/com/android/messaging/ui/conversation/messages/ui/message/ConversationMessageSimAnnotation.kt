package com.android.messaging.ui.conversation.messages.ui.message

import com.android.messaging.ui.conversation.messages.model.message.ConversationMessageUiModel

internal fun resolveConversationMessageSimDisplayName(
    message: ConversationMessageUiModel,
    messageBelow: ConversationMessageUiModel?,
    simDisplayNameByParticipantId: Map<String, String>,
): String? {
    val selfParticipantId = message.selfParticipantId
    val displayName = selfParticipantId?.let(simDisplayNameByParticipantId::get)

    val isLastInSimRun = messageBelow == null ||
        messageBelow.isIncoming != message.isIncoming ||
        messageBelow.selfParticipantId != selfParticipantId

    return when {
        simDisplayNameByParticipantId.size <= 1 -> null
        !isLastInSimRun -> null
        else -> displayName
    }
}
