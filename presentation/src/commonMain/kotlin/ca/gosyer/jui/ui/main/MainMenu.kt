/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.ui.base.navigation.BackHandler
import ca.gosyer.jui.ui.base.navigation.DisplayController
import ca.gosyer.jui.ui.base.navigation.withDisplayController
import ca.gosyer.jui.ui.main.components.BottomNav
import ca.gosyer.jui.ui.main.components.SideMenu
import ca.gosyer.jui.uicore.vm.LocalViewModelFactory
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

const val SIDE_MENU_EXPAND_DURATION = 500

@Composable
fun MainMenu() {
    val vmFactory = LocalViewModelFactory.current
    val vm = remember { vmFactory.instantiate<MainViewModel>() }
    DisposableEffect(vm) {
        onDispose(vm::onDispose)
    }
    val confirmExit by vm.confirmExit.collectAsState()
    Surface {
        Navigator(vm.startScreen.toScreen()) { navigator ->
            val controller = remember { DisplayController() }
            BoxWithConstraints {
                if (maxWidth > 720.dp) {
                    WideMainMenu(navigator, controller)
                } else {
                    SkinnyMainMenu(navigator)
                }
            }
            BackHandler(navigator.size == 1 && navigator.lastItem::class != vm.startScreen.toScreenClazz()) {
                navigator replaceAll vm.startScreen.toScreen()
            }
            var exitConfirmed by remember { mutableStateOf(false) }
            LaunchedEffect(exitConfirmed) {
                delay(2.seconds)
                exitConfirmed = false
            }
            BackHandler(
                confirmExit &&
                    navigator.size == 1 &&
                    navigator.lastItem::class == vm.startScreen.toScreenClazz() &&
                    !exitConfirmed
            ) {
                exitConfirmed = true
                vm.confirmExitToast()
            }
        }
    }
}

@Composable
fun SkinnyMainMenu(
    navigator: Navigator
) {
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                navigator.size <= 1,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                BottomNav(navigator)
            }
        }
    ) {
        Box(Modifier.padding(it)) {
            MainWindow(navigator, Modifier)
        }
    }
}

@Composable
fun WideMainMenu(
    navigator: Navigator,
    controller: DisplayController
) {
    Box {
        val startPadding by animateDpAsState(
            if (controller.sideMenuVisible) {
                200.dp
            } else {
                0.dp
            },
            animationSpec = tween(SIDE_MENU_EXPAND_DURATION)
        )
        if (startPadding != 0.dp) {
            SideMenu(Modifier.width(200.dp), controller, navigator)
        }
        withDisplayController(controller) {
            MainWindow(navigator, Modifier.padding(start = startPadding))
        }
    }
}

@Composable
fun MainWindow(navigator: Navigator, modifier: Modifier) {
    Surface(Modifier.fillMaxSize() then modifier) {
        FadeTransition(navigator)
    }
}
