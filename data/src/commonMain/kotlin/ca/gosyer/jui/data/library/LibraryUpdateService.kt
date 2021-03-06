/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.library

import ca.gosyer.jui.data.base.WebsocketService
import ca.gosyer.jui.data.library.model.UpdateStatus
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.updatesQuery
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.decodeFromString
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class LibraryUpdateService @Inject constructor(
    serverPreferences: ServerPreferences,
    client: Http
) : WebsocketService(serverPreferences, client) {

    override val _status: MutableStateFlow<Status> = MutableStateFlow(Status.STARTING)

    override val query: String
        get() = updatesQuery()

    override suspend fun onReceived(frame: Frame.Text) {
        val status = json.decodeFromString<UpdateStatus>(frame.readText())
        log.info { status }
    }

    private companion object {
        private val log = logging()
    }
}
