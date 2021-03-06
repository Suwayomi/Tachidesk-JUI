/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.manga.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.data.models.Category
import ca.gosyer.jui.data.models.Manga
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.ui.base.navigation.ActionItem
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.reader.rememberReaderLauncher
import ca.gosyer.jui.uicore.components.ErrorScreen
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.resources.stringResource
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant

@Composable
fun MangaScreenContent(
    isLoading: Boolean,
    manga: Manga?,
    chapters: List<ChapterDownloadItem>,
    dateTimeFormatter: (Instant) -> String,
    categoriesExist: Boolean,
    chooseCategoriesFlow: SharedFlow<Unit>,
    availableCategories: List<Category>,
    mangaCategories: List<Category>,
    addFavorite: (List<Category>, List<Category>) -> Unit,
    setCategories: () -> Unit,
    toggleFavorite: () -> Unit,
    refreshManga: () -> Unit,
    toggleRead: (Int) -> Unit,
    toggleBookmarked: (Int) -> Unit,
    markPreviousRead: (Int) -> Unit,
    downloadChapter: (Int) -> Unit,
    deleteDownload: (Int) -> Unit,
    stopDownloadingChapter: (Int) -> Unit,
    loadChapters: () -> Unit,
    loadManga: () -> Unit
) {
    val categoryDialogState = rememberMaterialDialogState()
    LaunchedEffect(Unit) {
        chooseCategoriesFlow.collect {
            categoryDialogState.show()
        }
    }
    val readerLauncher = rememberReaderLauncher()
    readerLauncher.Reader()

    Scaffold(
        topBar = {
            Toolbar(
                stringResource(MR.strings.location_manga),
                actions = {
                    val uriHandler = LocalUriHandler.current
                    getActionItems(
                        refreshManga = refreshManga,
                        refreshMangaEnabled = !isLoading,
                        categoryItemVisible = categoriesExist && manga?.inLibrary == true,
                        setCategories = setCategories,
                        inLibrary = manga?.inLibrary == true,
                        toggleFavorite = toggleFavorite,
                        favoritesButtonEnabled = manga != null,
                        openInBrowserEnabled = manga?.realUrl != null,
                        openInBrowser = {
                            manga?.realUrl?.let { uriHandler.openUri(it) }
                        }
                    )
                }
            )
        }
    ) {
        Box(Modifier.padding(it)) {
            manga.let { manga ->
                if (manga != null) {
                    Box {
                        val state = rememberLazyListState()
                        LazyColumn(state = state) {
                            item {
                                MangaItem(manga)
                            }
                            if (chapters.isNotEmpty()) {
                                items(chapters) { chapter ->
                                    ChapterItem(
                                        chapter,
                                        dateTimeFormatter,
                                        onClick = { readerLauncher.launch(it, manga.id) },
                                        toggleRead = toggleRead,
                                        toggleBookmarked = toggleBookmarked,
                                        markPreviousAsRead = markPreviousRead,
                                        onClickDownload = downloadChapter,
                                        onClickDeleteChapter = deleteDownload,
                                        onClickStopDownload = stopDownloadingChapter
                                    )
                                }
                            } else if (!isLoading) {
                                item {
                                    ErrorScreen(
                                        stringResource(MR.strings.no_chapters_found),
                                        Modifier.height(400.dp).fillMaxWidth(),
                                        retry = loadChapters
                                    )
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .scrollbarPadding(),
                            adapter = rememberScrollbarAdapter(state)
                        )
                    }
                } else if (!isLoading) {
                    ErrorScreen(stringResource(MR.strings.failed_manga_fetch), retry = loadManga)
                }
            }
            if (isLoading) {
                LoadingScreen()
            }
        }
    }
    CategorySelectDialog(categoryDialogState, availableCategories, mangaCategories, addFavorite)
}

@Composable
@Stable
private fun getActionItems(
    refreshManga: () -> Unit,
    refreshMangaEnabled: Boolean,
    categoryItemVisible: Boolean,
    setCategories: () -> Unit,
    inLibrary: Boolean,
    toggleFavorite: () -> Unit,
    favoritesButtonEnabled: Boolean,
    openInBrowserEnabled: Boolean,
    openInBrowser: () -> Unit
): List<ActionItem> {
    return listOfNotNull(
        ActionItem(
            name = stringResource(MR.strings.action_refresh_manga),
            icon = Icons.Rounded.Refresh,
            doAction = refreshManga,
            enabled = refreshMangaEnabled
        ),
        if (categoryItemVisible) {
            ActionItem(
                name = stringResource(MR.strings.edit_categories),
                icon = Icons.Rounded.Label,
                doAction = setCategories
            )
        } else null,
        ActionItem(
            name = stringResource(if (inLibrary) MR.strings.action_remove_favorite else MR.strings.action_favorite),
            icon = if (inLibrary) {
                Icons.Rounded.Favorite
            } else {
                Icons.Rounded.FavoriteBorder
            },
            doAction = toggleFavorite,
            enabled = favoritesButtonEnabled
        ),
        ActionItem(
            name = stringResource(MR.strings.action_browser),
            icon = Icons.Rounded.OpenInBrowser,
            enabled = openInBrowserEnabled,
            doAction = openInBrowser
        )
    )
}
