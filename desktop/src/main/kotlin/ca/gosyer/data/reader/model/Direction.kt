/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.reader.model

import ca.gosyer.i18n.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class Direction(@Transient val res: StringResource) {
    Down(MR.strings.dir_down),
    Left(MR.strings.dir_rtl),
    Right(MR.strings.dir_ltr),
    Up(MR.strings.dir_up)
}
