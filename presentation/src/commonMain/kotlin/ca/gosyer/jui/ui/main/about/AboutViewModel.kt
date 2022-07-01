/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about

import ca.gosyer.jui.data.base.DateHandler
import ca.gosyer.jui.domain.settings.interactor.AboutServer
import ca.gosyer.jui.domain.settings.model.About
import ca.gosyer.jui.domain.updates.interactor.UpdateChecker
import ca.gosyer.jui.domain.updates.interactor.UpdateChecker.Update
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class AboutViewModel @Inject constructor(
    private val dateHandler: DateHandler,
    private val aboutServer: AboutServer,
    private val updateChecker: UpdateChecker,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {

    private val _about = MutableStateFlow<About?>(null)
    val about = _about.asStateFlow()

    val formattedBuildTime = about.map { about ->
        about ?: return@map ""
        getFormattedDate(Instant.fromEpochSeconds(about.buildTime))
    }.stateIn(scope, SharingStarted.Eagerly, "")

    private val _updates = MutableSharedFlow<Update.UpdateFound>()
    val updates = _updates.asSharedFlow()

    init {
        getAbout()
    }

    private fun getAbout() {
        scope.launch {
            _about.value = aboutServer.await()
        }
    }

    fun checkForUpdates() {
        scope.launch {
            when (val update = updateChecker.await(true)) {
                is Update.UpdateFound -> _updates.emit(update)
                else -> Unit
            }
        }
    }

    private fun getFormattedDate(time: Instant): String {
        return dateHandler.dateTimeFormat(time)
    }

    companion object {
        private val log = logging()
    }
}
