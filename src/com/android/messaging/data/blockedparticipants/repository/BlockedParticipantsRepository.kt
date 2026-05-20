package com.android.messaging.data.blockedparticipants.repository

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import com.android.messaging.datamodel.DatabaseHelper.ParticipantColumns
import com.android.messaging.datamodel.MessagingContentProvider
import com.android.messaging.datamodel.data.ParticipantData
import com.android.messaging.di.core.MessagingDbDispatcher
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal interface BlockedParticipantsRepository {
    fun observeBlockedParticipants(): Flow<ImmutableList<ParticipantData>>
}

internal class BlockedParticipantsRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @param:MessagingDbDispatcher private val messagingDbDispatcher: CoroutineDispatcher,
) : BlockedParticipantsRepository {

    override fun observeBlockedParticipants(): Flow<ImmutableList<ParticipantData>> {
        return observeUri(uri = MessagingContentProvider.PARTICIPANTS_URI)
            .map { queryBlockedParticipants() }
            .flowOn(messagingDbDispatcher)
    }

    private fun queryBlockedParticipants(): ImmutableList<ParticipantData> {
        val cursor = contentResolver.query(
            MessagingContentProvider.PARTICIPANTS_URI,
            ParticipantData.ParticipantsQuery.PROJECTION,
            "${ParticipantColumns.BLOCKED}=1",
            null,
            null,
        ) ?: return persistentListOf()

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(ParticipantData.getFromCursor(it))
                }
            }.toImmutableList()
        }
    }

    private fun observeUri(uri: Uri): Flow<Unit> {
        return callbackFlow {
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    trySend(Unit)
                }
            }
            contentResolver.registerContentObserver(uri, true, observer)

            trySend(Unit)

            awaitClose {
                contentResolver.unregisterContentObserver(observer)
            }
        }
    }
}
