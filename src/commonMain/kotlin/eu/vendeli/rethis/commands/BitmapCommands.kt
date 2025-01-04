package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.options.BitOpOption
import eu.vendeli.rethis.types.options.BitcountOption
import eu.vendeli.rethis.types.options.BitfieldOption
import eu.vendeli.rethis.types.options.BitmapDataType
import eu.vendeli.rethis.utils.writeArg

suspend fun ReThis.bitCount(
    key: String,
    range: BitcountOption.Range? = null,
    mode: BitmapDataType? = null,
): Long = execute<Long>(
    mutableListOf("BITCOUNT".toArg(), key.toArg()).apply {
        range?.let { writeArg(it) }
        mode?.let { writeArg(it) }
    },
) ?: 0

suspend fun ReThis.bitfield(
    key: String,
    vararg options: BitfieldOption,
): List<Long>? = execute<Long>(
    mutableListOf("BITFIELD".toArg(), key.toArg()).writeArg(options),
    isCollectionResponse = true,
)

suspend fun ReThis.bitfieldRO(
    key: String,
    vararg options: BitfieldOption.GET,
): List<Long> = execute(
    mutableListOf("BITFIELD_RO".toArg(), key.toArg()).writeArg(options),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.bitOp(
    type: BitOpOption.OperationType,
    destKey: String,
    vararg keys: String,
): Long = execute<Long>(
    listOf("BITOP".toArg(), type.toArg(), destKey.toArg(), *keys.toArg()),
) ?: 0

suspend fun ReThis.bitPos(
    key: String,
    bit: Long,
    start: Long? = null,
    end: Long? = null,
    type: BitOpOption.OperationType? = null,
): Long = execute<Long>(
    mutableListOf("BITPOS".toArg(), key.toArg(), bit.toArg()).apply {
        start?.let { writeArg(it) }
        end?.let { writeArg(it) }
        writeArg(type)
    },
) ?: 0

suspend fun ReThis.getBit(key: String, offset: Long): Long = execute<Long>(
    listOf("GETBIT".toArg(), key.toArg(), offset.toArg()),
) ?: 0

suspend fun ReThis.setBit(key: String, offset: Long, value: Long): Long = execute<Long>(
    listOf("SETBIT".toArg(), key.toArg(), offset.toArg(), value.toArg()),
) ?: 0
