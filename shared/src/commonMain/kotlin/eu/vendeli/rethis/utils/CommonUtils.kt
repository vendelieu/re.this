@file:JvmName("SharedCommonUtilsKt")

package eu.vendeli.rethis.utils

import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import kotlin.jvm.JvmName
import kotlinx.io.Buffer

fun Buffer.parseCode(default: RespCode) =
    if (this == EMPTY_BUFFER) default else RespCode.fromCode(readByte())
