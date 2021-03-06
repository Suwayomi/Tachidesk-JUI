/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.reader.model

import ca.gosyer.jui.i18n.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class NavigationMode(@Transient val res: StringResource) {
    LNavigation(MR.strings.nav_l_shaped),
    KindlishNavigation(MR.strings.nav_kindle_ish),
    EdgeNavigation(MR.strings.nav_edge),
    RightAndLeftNavigation(MR.strings.nav_left_right),
}
