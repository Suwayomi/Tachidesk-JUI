/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.base

import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.service.ServerPreferences
import io.ktor.http.URLBuilder

open class BaseRepository(
    protected val client: Http,
    serverPreferences: ServerPreferences
) {
    private val _serverUrl = serverPreferences.serverUrl()
    val serverUrl get() = _serverUrl.get().toString()

    fun buildUrl(builder: URLBuilder.() -> Unit) = URLBuilder(serverUrl).apply(builder).buildString()

    fun URLBuilder.parameter(key: String, value: Any) = encodedParameters.append(key, value.toString())
}