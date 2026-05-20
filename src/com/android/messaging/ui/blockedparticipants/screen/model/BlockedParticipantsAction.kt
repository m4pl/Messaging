package com.android.messaging.ui.blockedparticipants.screen.model

internal sealed interface BlockedParticipantsAction {

    data class UnblockClicked(
        val normalizedDestination: String,
    ) : BlockedParticipantsAction
}
