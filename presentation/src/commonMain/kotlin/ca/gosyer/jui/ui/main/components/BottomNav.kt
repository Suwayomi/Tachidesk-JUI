/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.components

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.ui.main.TopLevelMenus
import ca.gosyer.jui.uicore.resources.stringResource
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun BottomNav(navigator: Navigator) {
    BottomNavigation {
        remember { TopLevelMenus.values().asList() }.fastForEach {
            val isSelected = it.isSelected(navigator)
            BottomNavigationItem(
                selected = isSelected,
                onClick = {
                    if (isSelected) return@BottomNavigationItem
                    navigator replace it.createScreen()
                },
                icon = {
                    if (isSelected) {
                        Icon(it.selectedIcon, stringResource(it.textKey))
                    } else {
                        Icon(it.unselectedIcon, stringResource(it.textKey))
                    }
                },
                label = {
                    Text(
                        text = stringResource(it.textKey),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}
