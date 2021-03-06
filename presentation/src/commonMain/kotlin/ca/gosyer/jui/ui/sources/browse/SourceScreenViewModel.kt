/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.browse

import ca.gosyer.jui.data.catalog.CatalogPreferences
import ca.gosyer.jui.data.library.LibraryPreferences
import ca.gosyer.jui.data.library.model.DisplayMode
import ca.gosyer.jui.data.models.Manga
import ca.gosyer.jui.data.models.MangaPage
import ca.gosyer.jui.data.models.Source
import ca.gosyer.jui.data.server.interactions.SourceInteractionHandler
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class SourceScreenViewModel(
    private val source: Source,
    private val sourceHandler: SourceInteractionHandler,
    private val catalogPreferences: CatalogPreferences,
    private val libraryPreferences: LibraryPreferences,
    contextWrapper: ContextWrapper,
    initialQuery: String?
) : ViewModel(contextWrapper) {

    @Inject constructor(
        sourceHandler: SourceInteractionHandler,
        catalogPreferences: CatalogPreferences,
        libraryPreferences: LibraryPreferences,
        contextWrapper: ContextWrapper,
        params: Params
    ) : this(
        params.source,
        sourceHandler,
        catalogPreferences,
        libraryPreferences,
        contextWrapper,
        params.initialQuery
    )

    val displayMode = catalogPreferences.displayMode().stateIn(scope)
    val gridColumns = libraryPreferences.gridColumns().stateIn(scope)
    val gridSize = libraryPreferences.gridSize().stateIn(scope)

    private val _mangas = MutableStateFlow(emptyList<Manga>())
    val mangas = _mangas.asStateFlow()

    private val _hasNextPage = MutableStateFlow(false)
    val hasNextPage = _hasNextPage.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _isLatest = MutableStateFlow(false)
    val isLatest = _isLatest.asStateFlow()

    private val _latestButtonEnabled = MutableStateFlow(false)
    val latestButtonEnabled = _latestButtonEnabled.asStateFlow()

    private val _usingFilters = MutableStateFlow(false)

    private val _sourceSearchQuery = MutableStateFlow(initialQuery)
    val sourceSearchQuery = _sourceSearchQuery.asStateFlow()

    private val _query = MutableStateFlow(sourceSearchQuery.value)

    private val _pageNum = MutableStateFlow(1)
    val pageNum = _pageNum.asStateFlow()

    private val sourceMutex = Mutex()

    init {
        scope.launch {
            getPage()?.let { (mangas, hasNextPage) ->
                _mangas.value = mangas
                _hasNextPage.value = hasNextPage
            }

            _loading.value = false
        }
    }

    fun loadNextPage() {
        scope.launch {
            if (hasNextPage.value && sourceMutex.tryLock()) {
                _pageNum.value++
                val page = getPage()
                if (page != null) {
                    _mangas.value += page.mangaList
                    _hasNextPage.value = page.hasNextPage
                } else {
                    _pageNum.value--
                }
                sourceMutex.unlock()
            }
            _loading.value = false
        }
    }

    fun setMode(toLatest: Boolean) {
        if (isLatest.value != toLatest) {
            _isLatest.value = toLatest
            // [loadNextPage] increments by 1
            _pageNum.value = 0
            _loading.value = true
            _query.value = null
            _mangas.value = emptyList()
            loadNextPage()
        }
    }

    private suspend fun getPage(): MangaPage? {
        return when {
            isLatest.value -> sourceHandler.getLatestManga(source, pageNum.value)
            _query.value != null || _usingFilters.value -> sourceHandler.getSearchResults(source, _query.value.orEmpty(), pageNum.value)
            else -> sourceHandler.getPopularManga(source, pageNum.value)
        }
            .catch {
                log.warn(it) { "Error getting source page" }
            }
            .singleOrNull()
    }

    fun startSearch(query: String?) {
        _pageNum.value = 0
        _hasNextPage.value = true
        _loading.value = true
        _query.value = query
        _mangas.value = emptyList()
        loadNextPage()
    }

    fun setUsingFilters(usingFilters: Boolean) {
        _usingFilters.value = usingFilters
    }
    fun enableLatest(enabled: Boolean) {
        _latestButtonEnabled.value = enabled
    }

    fun search(query: String) {
        _sourceSearchQuery.value = query
    }
    fun submitSearch() {
        startSearch(sourceSearchQuery.value)
    }

    fun selectDisplayMode(displayMode: DisplayMode) {
        catalogPreferences.displayMode().set(displayMode)
    }

    data class Params(val source: Source, val initialQuery: String?)

    private companion object {
        private val log = logging()
    }
}
