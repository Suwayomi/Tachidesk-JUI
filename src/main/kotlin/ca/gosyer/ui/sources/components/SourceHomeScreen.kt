/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.components.KamelImage
import ca.gosyer.ui.base.components.LoadingScreen
import io.kamel.image.lazyPainterResource

@Composable
fun SourceHomeScreen(
    isLoading: Boolean,
    sources: List<Source>,
    onSourceClicked: (Source) -> Unit
) {
    if (sources.isEmpty()) {
        LoadingScreen(isLoading)
    } else {
        Box(Modifier.fillMaxSize(), Alignment.TopCenter) {
            val state = rememberLazyListState()
            SourceCategory(sources, onSourceClicked, state)
            /*val sourcesByLang = sources.groupBy { it.lang.toLowerCase() }.toList()
            LazyColumn(state = state) {
                items(sourcesByLang) { (lang, sources) ->
                    SourceCategory(
                        lang,
                        sources,
                        onSourceClicked = sourceClicked
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }*/

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(state)
            )
        }
    }
}

@Composable
fun SourceCategory(
    sources: List<Source>,
    onSourceClicked: (Source) -> Unit,
    state: LazyListState
) {
    LazyVerticalGrid(GridCells.Adaptive(120.dp), state = state) {
        items(sources) { source ->
            SourceItem(
                source,
                onSourceClicked = onSourceClicked
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun SourceItem(
    source: Source,
    onSourceClicked: (Source) -> Unit
) {
    TooltipArea(
        {
            Surface(
                modifier = Modifier.shadow(4.dp),
                shape = RoundedCornerShape(4.dp),
                elevation = 4.dp
            ) {
                Text(source.name, modifier = Modifier.padding(10.dp))
            }
        }
    ) {
        Column(
            Modifier.width(120.dp)
                .defaultMinSize(minHeight = 120.dp)
                .padding(8.dp)
                .clickable {
                    onSourceClicked(source)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KamelImage(lazyPainterResource(source, filterQuality = FilterQuality.Medium), source.displayName, Modifier.size(96.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                "${source.name} (${source.lang.uppercase()})",
                color = MaterialTheme.colors.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
