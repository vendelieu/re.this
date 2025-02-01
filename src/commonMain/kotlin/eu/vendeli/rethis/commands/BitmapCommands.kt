package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.toArgument
import eu.vendeli.rethis.types.options.BitOpOption
import eu.vendeli.rethis.types.options.BitcountOption
import eu.vendeli.rethis.types.options.BitfieldOption
import eu.vendeli.rethis.types.options.BitmapDataType
import eu.vendeli.rethis.utils.writeArgument
import eu.vendeli.rethis.utils.execute

suspend fun ReThis.bitCount(
    key: String,
    range: BitcountOption.Range? = null,
    mode: BitmapDataType? = null,
): Long = execute<Long>(
    mutableListOf("BITCOUNT".toArgument(), key.toArgument()).apply {
        range?.let { writeArgument(it) }
        mode?.let { writeArgument(it) }
    },
) ?: 0

suspend fun ReThis.bitfield(
    key: String,
    vararg options: BitfieldOption,
): List<Long>? = execute<Long>(
    mutableListOf("BITFIELD".toArgument(), key.toArgument()).writeArgument(options),
    isCollectionResponse = true,
)

suspend fun ReThis.bitfieldRO(
    key: String,
    vararg options: BitfieldOption.GET,
): List<Long> = execute(
    mutableListOf("BITFIELD_RO".toArgument(), key.toArgument()).writeArgument(options),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.bitOp(
    type: BitOpOption.OperationType,
    destKey: String,
    vararg keys: String,
): Long = execute<Long>(
    listOf("BITOP".toArgument(), type.toArgument(), destKey.toArgument(), *keys.toArgument()),
) ?: 0

suspend fun ReThis.bitPos(
    key: String,
    bit: Long,
    start: Long? = null,
    end: Long? = null,
    type: BitOpOption.OperationType? = null,
): Long = execute<Long>(
    mutableListOf("BITPOS".toArgument(), key.toArgument(), bit.toArgument()).apply {
        start?.let { writeArgument(it) }
        end?.let { writeArgument(it) }
        writeArgument(type)
    },
) ?: 0

suspend fun ReThis.getBit(key: String, offset: Long): Long = execute<Long>(
    listOf("GETBIT".toArgument(), key.toArgument(), offset.toArgument()),
) ?: 0

suspend fun ReThis.setBit(key: String, offset: Long, value: Long): Long = execute<Long>(
    listOf("SETBIT".toArgument(), key.toArgument(), offset.toArgument(), value.toArgument()),
) ?: 0
