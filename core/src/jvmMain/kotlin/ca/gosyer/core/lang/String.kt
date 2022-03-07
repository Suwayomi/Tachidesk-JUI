/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
@file:JvmName("JvmStringsKt")

package ca.gosyer.core.lang

import java.util.Locale

fun String.capitalize(locale: Locale = Locale.getDefault()) =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
