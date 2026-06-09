package com.android.messaging.ui.conversationpicker.delegate

import com.android.messaging.data.contact.repository.ContactsRepository
import com.android.messaging.data.conversationpicker.model.TargetConversation
import com.android.messaging.data.conversationpicker.repository.TargetsRepository
import com.android.messaging.di.core.DefaultDispatcher
import com.android.messaging.domain.contacts.usecase.IsReadContactsPermissionGranted
import com.android.messaging.ui.conversationpicker.mapper.ContactSectionMapper
import com.android.messaging.ui.conversationpicker.mapper.ContactUiStateMapper
import com.android.messaging.ui.conversationpicker.mapper.TargetUiStateMapper
import com.android.messaging.ui.conversationpicker.model.ContactSection
import com.android.messaging.ui.conversationpicker.model.ContactTargetsUiState
import com.android.messaging.ui.conversationpicker.model.RecentTargetsUiState
import com.android.messaging.ui.conversationpicker.model.SelectionUiState
import com.android.messaging.ui.conversationpicker.model.TargetUiState
import com.android.messaging.ui.conversationpicker.model.TargetsUiState
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface TargetsDelegate {
    val state: StateFlow<TargetsUiState>
    val selectedIds: Flow<ImmutableSet<String>>
    fun bind(scope: CoroutineScope)
    fun setSearchActive(active: Boolean)
    fun setSearchQuery(query: String)
    fun toggleSelection(target: TargetUiState)
    fun clearSelection()
    fun loadMoreRecent()
    fun collapseRecent()
    fun loadMoreContacts()
    fun onContactsPermissionGranted()
    fun currentSelectedTargets(): ImmutableList<TargetUiState>
}

internal class TargetsDelegateImpl @Inject constructor(
    private val repository: TargetsRepository,
    private val contactsRepository: ContactsRepository,
    private val conversationMapper: TargetUiStateMapper,
    private val contactMapper: ContactUiStateMapper,
    private val contactSectionMapper: ContactSectionMapper,
    private val isReadContactsPermissionGranted: IsReadContactsPermissionGranted,
    @param:DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
) : TargetsDelegate {

    private val recents = MutableStateFlow<ImmutableList<TargetConversation>?>(null)
    private val contactsState = MutableStateFlow(ContactsState())
    private val searchQuery = MutableStateFlow("")
    private val isSearchActive = MutableStateFlow(false)
    private val visibleRecentLimit = MutableStateFlow(INITIAL_RECENT_TARGET_COUNT)
    private val contactsReloadTrigger = MutableStateFlow(0)
    private val selectedTargetsList = MutableStateFlow<PersistentList<TargetUiState>>(
        persistentListOf(),
    )

    private var boundScope: CoroutineScope? = null

    private val _state = MutableStateFlow(TargetsUiState())
    override val state: StateFlow<TargetsUiState> = _state.asStateFlow()

    override val selectedIds: Flow<ImmutableSet<String>> = selectedTargetsList.map {
        it.toSelectionIds()
    }

    override fun bind(scope: CoroutineScope) {
        if (boundScope != null) return
        boundScope = scope

        scope.launch(defaultDispatcher) {
            combine(
                combine(recents, searchQuery, visibleRecentLimit, ::buildRecentTargets),
                contactsState,
                isSearchActive,
                selectedTargetsList,
            ) { recentTargets, contacts, active, selectedTargets ->
                buildState(
                    recentTargets = recentTargets,
                    contacts = contacts,
                    isSearchActive = active,
                    selectedTargets = selectedTargets,
                )
            }.collect { state ->
                _state.value = state
            }
        }

        scope.launch(defaultDispatcher) {
            repository.observeTargets()
                .collect { conversations ->
                    recents.value = conversations
                    pruneSelection(conversations)
                }
        }

        scope.launch(defaultDispatcher) {
            combine(
                searchQuery,
                contactsReloadTrigger
            ) { query, _ -> query }
                .collectLatest(::loadInitialContacts)
        }
    }

    override fun setSearchActive(active: Boolean) {
        isSearchActive.value = active

        if (!active) {
            searchQuery.value = ""
        }
    }

    override fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    override fun toggleSelection(target: TargetUiState) {
        val current = selectedTargetsList.value
        val isSelected = current.any { it.selectionId == target.selectionId }

        selectedTargetsList.value = when {
            isSelected -> current.removeAll { it.selectionId == target.selectionId }
            else -> current.add(target)
        }
    }

    override fun clearSelection() {
        selectedTargetsList.value = persistentListOf()
    }

    override fun loadMoreRecent() {
        visibleRecentLimit.update { it + RECENT_TARGET_LOAD_MORE_COUNT }
    }

    override fun collapseRecent() {
        visibleRecentLimit.value = INITIAL_RECENT_TARGET_COUNT
    }

    override fun loadMoreContacts() {
        boundScope?.launch(defaultDispatcher) {
            loadMoreContactsPage()
        }
    }

    override fun onContactsPermissionGranted() {
        if (!contactsState.value.hasPermission) {
            contactsReloadTrigger.update { it + 1 }
        }
    }

    override fun currentSelectedTargets(): ImmutableList<TargetUiState> {
        return selectedTargetsList.value
    }

    private fun buildState(
        recentTargets: RecentTargetsState,
        contacts: ContactsState,
        isSearchActive: Boolean,
        selectedTargets: PersistentList<TargetUiState>,
    ): TargetsUiState {
        val selection = SelectionUiState(
            selectedIds = selectedTargets.toSelectionIds(),
            selectedTargets = selectedTargets,
        )

        if (recentTargets.isLoading) {
            return TargetsUiState(
                isLoading = true,
                isSearchActive = isSearchActive,
                contacts = ContactTargetsUiState(hasPermission = contacts.hasPermission),
                selection = selection,
            )
        }

        return TargetsUiState(
            isLoading = false,
            isSearchActive = isSearchActive,
            recent = RecentTargetsUiState(
                targets = recentTargets.targets,
                canLoadMore = recentTargets.canLoadMore,
                canCollapse = recentTargets.canCollapse,
            ),
            contacts = ContactTargetsUiState(
                sections = contacts.sections,
                hasPermission = contacts.hasPermission,
                canLoadMore = contacts.canLoadMore,
            ),
            selection = selection,
        )
    }

    private fun buildRecentTargets(
        recentConversations: ImmutableList<TargetConversation>?,
        query: String,
        visibleLimit: Int,
    ): RecentTargetsState {
        if (recentConversations == null) {
            return RecentTargetsState(isLoading = true)
        }

        return when {
            query.isNotBlank() -> {
                RecentTargetsState(
                    targets = conversationMapper.map(recentConversations).filterByQuery(query),
                )
            }

            else -> {
                val visibleConversations = recentConversations
                    .take(visibleLimit)
                    .toImmutableList()

                RecentTargetsState(
                    targets = conversationMapper.map(visibleConversations),
                    canLoadMore = visibleConversations.size < recentConversations.size,
                    canCollapse = visibleLimit > INITIAL_RECENT_TARGET_COUNT,
                )
            }
        }
    }

    private suspend fun loadInitialContacts(query: String) {
        if (!isReadContactsPermissionGranted()) {
            contactsState.value = ContactsState(hasPermission = false)
            return
        }

        delay(SEARCH_DEBOUNCE)

        val page = contactsRepository.searchContacts(
            query = query,
            offset = 0
        ).first()

        val contacts = contactMapper.map(page.contacts)

        contactsState.value = ContactsState(
            contacts = contacts,
            sections = contactSectionMapper.map(contacts),
            hasPermission = true,
            canLoadMore = page.nextOffset != null,
            nextOffset = page.nextOffset,
            loadedQuery = query,
        )
    }

    private suspend fun loadMoreContactsPage() {
        val current = contactsState.value
        val nextOffset = current.nextOffset

        if (!current.hasPermission || current.isLoadingMore || nextOffset == null) {
            return
        }

        contactsState.update {
            it.copy(isLoadingMore = true)
        }

        val page = contactsRepository
            .searchContacts(
                query = current.loadedQuery,
                offset = nextOffset
            )
            .first()

        if (searchQuery.value != current.loadedQuery) {
            contactsState.update {
                it.copy(isLoadingMore = false)
            }
            return
        }

        val additional = contactMapper.map(page.contacts)

        contactsState.update { state ->
            val mergedContacts = mergeContacts(state.contacts, additional)

            state.copy(
                contacts = mergedContacts,
                sections = contactSectionMapper.map(mergedContacts),
                canLoadMore = page.nextOffset != null,
                nextOffset = page.nextOffset,
                isLoadingMore = false,
            )
        }
    }

    private fun mergeContacts(
        existing: ImmutableList<TargetUiState.Contact>,
        additional: ImmutableList<TargetUiState.Contact>,
    ): ImmutableList<TargetUiState.Contact> {
        val seenKeys = existing.mapTo(HashSet()) { it.key }

        return (existing + additional.filter { seenKeys.add(it.key) })
            .toImmutableList()
    }

    private fun pruneSelection(availableRecents: ImmutableList<TargetConversation>) {
        if (selectedTargetsList.value.isEmpty()) {
            return
        }

        val availableConversationIds = availableRecents.mapTo(HashSet()) { it.conversationId }

        selectedTargetsList.update { selected ->
            selected.removeAll { target ->
                target is TargetUiState.Conversation &&
                    target.conversationId !in availableConversationIds
            }
        }
    }

    private fun List<TargetUiState>.toSelectionIds(): ImmutableSet<String> {
        return map { it.selectionId }.toPersistentSet()
    }

    private fun ImmutableList<TargetUiState>.filterByQuery(
        query: String,
    ): ImmutableList<TargetUiState> {
        if (query.isBlank()) {
            return this
        }

        return filter { target ->
            target.matches(query)
        }.toImmutableList()
    }

    private fun TargetUiState.matches(query: String): Boolean {
        return displayName.contains(query, ignoreCase = true) ||
            details?.contains(query, ignoreCase = true) == true
    }

    private data class RecentTargetsState(
        val targets: ImmutableList<TargetUiState> = persistentListOf(),
        val canLoadMore: Boolean = false,
        val canCollapse: Boolean = false,
        val isLoading: Boolean = false,
    )

    private data class ContactsState(
        val contacts: ImmutableList<TargetUiState.Contact> = persistentListOf(),
        val sections: ImmutableList<ContactSection> = persistentListOf(),
        val hasPermission: Boolean = true,
        val canLoadMore: Boolean = false,
        val isLoadingMore: Boolean = false,
        val nextOffset: Int? = null,
        val loadedQuery: String = "",
    )

    private companion object {
        private const val INITIAL_RECENT_TARGET_COUNT = 5
        private const val RECENT_TARGET_LOAD_MORE_COUNT = 15
        private val SEARCH_DEBOUNCE = 150L.milliseconds
    }
}
