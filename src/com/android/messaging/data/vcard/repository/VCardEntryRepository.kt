package com.android.messaging.data.vcard.repository

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.provider.ContactsContract.Contacts
import androidx.core.net.toUri
import com.android.messaging.data.vcard.parser.VCardParser
import com.android.messaging.datamodel.media.CustomVCardEntry
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal interface VCardEntryRepository {
    fun observeEntries(
        vCardUri: String,
        refreshes: Flow<Unit> = emptyFlow(),
    ): Flow<List<CustomVCardEntry>>

    suspend fun getEntries(vCardUri: String): List<CustomVCardEntry>
}

@Singleton
internal class VCardEntryRepositoryImpl @Inject constructor(
    private val parser: VCardParser,
    private val contentResolver: ContentResolver,
) : VCardEntryRepository {

    private val lock = Mutex()
    private val pendingParseResults =
        mutableMapOf<String, CompletableDeferred<List<CustomVCardEntry>>>()
    private val cachedEntries = object : LinkedHashMap<String, List<CustomVCardEntry>>(
        MAX_CACHED_VCARDS,
        LOAD_FACTOR,
        true,
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<String, List<CustomVCardEntry>>,
        ): Boolean {
            return size > MAX_CACHED_VCARDS
        }
    }

    override fun observeEntries(
        vCardUri: String,
        refreshes: Flow<Unit>,
    ): Flow<List<CustomVCardEntry>> {
        if (vCardUri.isBlank()) {
            return flowOf(emptyList())
        }

        return merge(
            refreshEvents(vCardUri),
            refreshes,
        ).map {
            getEntries(vCardUri)
        }
    }

    override suspend fun getEntries(vCardUri: String): List<CustomVCardEntry> {
        return when {
            vCardUri.isBlank() -> emptyList()

            else -> {
                val cacheEntries = shouldCache(vCardUri)
                val cachedEntries = when {
                    cacheEntries -> cached(vCardUri)
                    else -> null
                }

                cachedEntries ?: parseShared(vCardUri, cacheEntries)
            }
        }
    }

    private suspend fun parseShared(
        vCardUri: String,
        shouldCache: Boolean,
    ): List<CustomVCardEntry> {
        val parseHandle = getOrCreateParseHandle(vCardUri)
        if (!parseHandle.isOwner) {
            return parseHandle.deferred.await()
        }

        val result = runCatching {
            val entries = parser.parse(vCardUri)
            cacheIfNeeded(
                vCardUri = vCardUri,
                entries = entries,
                shouldCache = shouldCache,
            )
            entries
        }

        result.onSuccess { entries ->
            parseHandle.deferred.complete(entries)
        }.onFailure { throwable ->
            parseHandle.deferred.completeExceptionally(throwable)
        }

        clearParseHandle(vCardUri, parseHandle.deferred)
        return result.getOrThrow()
    }

    private suspend fun cacheIfNeeded(
        vCardUri: String,
        entries: List<CustomVCardEntry>,
        shouldCache: Boolean,
    ) {
        if (!shouldCache || entries.isEmpty()) {
            return
        }

        lock.withLock {
            cachedEntries[vCardUri] = entries
        }
    }

    private fun refreshEvents(vCardUri: String): Flow<Unit> {
        return when {
            vCardUri.toUri().isContactVCardUri() -> contactChangeEvents().conflate()
            else -> flowOf(Unit)
        }
    }

    private fun contactChangeEvents(): Flow<Unit> {
        return callbackFlow {
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    trySend(Unit)
                }
            }

            contentResolver.registerContentObserver(Contacts.CONTENT_URI, true, observer)
            trySend(Unit)

            awaitClose {
                contentResolver.unregisterContentObserver(observer)
            }
        }
    }

    private fun shouldCache(vCardUri: String): Boolean {
        return !vCardUri.toUri().isContactVCardUri()
    }

    private fun Uri.isContactVCardUri(): Boolean {
        val contactVCardUri = Contacts.CONTENT_VCARD_URI

        return scheme == contactVCardUri.scheme &&
            authority == contactVCardUri.authority &&
            pathSegments.take(contactVCardUri.pathSegments.size) == contactVCardUri.pathSegments
    }

    private suspend fun cached(vCardUri: String): List<CustomVCardEntry>? {
        return lock.withLock {
            cachedEntries[vCardUri]
        }
    }

    private suspend fun getOrCreateParseHandle(vCardUri: String): ParseHandle {
        return lock.withLock {
            val existing = pendingParseResults[vCardUri]
            if (existing != null) {
                return@withLock ParseHandle(
                    deferred = existing,
                    isOwner = false,
                )
            }

            CompletableDeferred<List<CustomVCardEntry>>().let { deferred ->
                pendingParseResults[vCardUri] = deferred
                ParseHandle(
                    deferred = deferred,
                    isOwner = true,
                )
            }
        }
    }

    private suspend fun clearParseHandle(
        vCardUri: String,
        deferred: CompletableDeferred<List<CustomVCardEntry>>,
    ) {
        lock.withLock {
            if (pendingParseResults[vCardUri] === deferred) {
                pendingParseResults.remove(vCardUri)
            }
        }
    }

    private data class ParseHandle(
        val deferred: CompletableDeferred<List<CustomVCardEntry>>,
        val isOwner: Boolean,
    )

    private companion object {
        private const val MAX_CACHED_VCARDS = 5
        private const val LOAD_FACTOR = 0.75f
    }
}
