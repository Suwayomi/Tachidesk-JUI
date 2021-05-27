/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader

import ca.gosyer.data.reader.ReaderModeWatch
import ca.gosyer.data.reader.ReaderPreferences
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.ui.reader.model.ReaderPage
import ca.gosyer.ui.reader.model.ViewerChapters
import ca.gosyer.util.lang.throwIfCancellation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReaderMenuViewModel @Inject constructor(
    params: Params,
    private val readerPreferences: ReaderPreferences,
    private val chapterHandler: ChapterInteractionHandler
) : ViewModel() {
    private val viewerChapters = ViewerChapters(
        MutableStateFlow(null),
        MutableStateFlow(null),
        MutableStateFlow(null)
    )
    val previousChapter = viewerChapters.prevChapter.asStateFlow()
    val chapter = viewerChapters.currChapter.asStateFlow()
    val nextChapter = viewerChapters.nextChapter.asStateFlow()

    private val _state = MutableStateFlow<ReaderChapter.State>(ReaderChapter.State.Wait)
    val state = _state.asStateFlow()

    private val _pages = MutableStateFlow(emptyList<ReaderPage>())
    val pages = _pages.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    val readerModeSettings = ReaderModeWatch(readerPreferences, scope)

    private val loader = ChapterLoader(scope.coroutineContext, readerPreferences, chapterHandler)

    init {
        scope.launch(Dispatchers.Default) {
            init(params.mangaId, params.chapterIndex)
        }
    }

    fun progress(index: Int) {
        _currentPage.value = index
    }

    fun retry(page: ReaderPage) {
        chapter.value?.pageLoader?.retryPage(page)
    }

    private fun resetValues() {
        _pages.value = emptyList()
        _currentPage.value = 1
        _state.value = ReaderChapter.State.Wait
        viewerChapters.recycle()
    }

    suspend fun init(mangaId: Long, chapterIndex: Int) {
        resetValues()
        val chapter = ReaderChapter(
            scope.coroutineContext + Dispatchers.Default,
            chapterHandler.getChapter(mangaId, chapterIndex)
        )
        val pages = loader.loadChapter(chapter)
        viewerChapters.currChapter.value = chapter
        scope.launch(Dispatchers.Default) {
            listOf(
                async {
                    try {
                        viewerChapters.nextChapter.value = ReaderChapter(
                            scope.coroutineContext + Dispatchers.Default,
                            chapterHandler.getChapter(mangaId, chapterIndex + 1)
                        )
                    } catch (e: Exception) {
                        e.throwIfCancellation()
                    }
                },
                async {
                    if (chapterIndex != 0) {
                        try {
                            viewerChapters.prevChapter.value = ReaderChapter(
                                scope.coroutineContext + Dispatchers.Default,
                                chapterHandler.getChapter(mangaId, chapterIndex - 1)
                            )
                        } catch (e: Exception) {
                            e.throwIfCancellation()
                        }
                    }
                }
            ).awaitAll()
        }
        chapter.stateObserver.onEach {
            _state.value = it
        }.launchIn(chapter.scope)
        pages.onEach { pageList ->
            pageList.forEach { it.chapter = chapter }
            _pages.value = pageList
        }.launchIn(chapter.scope)
        _currentPage.onEach { index ->
            pages.value.getOrNull(index - 1)?.let { chapter.pageLoader?.loadPage(it) }
        }.launchIn(chapter.scope)
    }

    data class Params(val chapterIndex: Int, val mangaId: Long)
}