package com.android.messaging.data.blockedparticipants.repository

import com.android.messaging.datamodel.action.UpdateDestinationBlockedAction.UpdateDestinationBlockedActionListener
import com.android.messaging.datamodel.action.UpdateDestinationBlockedAction.updateDestinationBlocked
import com.android.messaging.di.core.MainDispatcher
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal interface BlockedParticipantsRepository {
    suspend fun setDestinationBlocked(
        destination: String,
        conversationId: String?,
        isBlocked: Boolean,
    ): Boolean
}

internal class BlockedParticipantsRepositoryImpl @Inject constructor(
    @param:MainDispatcher
    private val mainDispatcher: CoroutineDispatcher,
) : BlockedParticipantsRepository {

    override suspend fun setDestinationBlocked(
        destination: String,
        conversationId: String?,
        isBlocked: Boolean,
    ): Boolean {
        val resolvedDestination = destination.takeIf(String::isNotBlank) ?: return false

        return withContext(mainDispatcher) {
            suspendCancellableCoroutine { continuation ->
                val listener = UpdateDestinationBlockedActionListener { _, success, _, _ ->
                    if (continuation.isActive) {
                        continuation.resume(success)
                    }
                }

                val actionMonitor = updateDestinationBlocked(
                    resolvedDestination,
                    isBlocked,
                    conversationId?.takeIf(String::isNotBlank),
                    listener,
                )

                continuation.invokeOnCancellation {
                    actionMonitor?.unregister()
                }
            }
        }
    }
}
