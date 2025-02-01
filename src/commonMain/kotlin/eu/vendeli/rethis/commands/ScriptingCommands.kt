package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArgument
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.options.FunctionRestoreOption
import eu.vendeli.rethis.utils.response.unwrapRespIndMap
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.writeArgument
import eu.vendeli.rethis.utils.execute

suspend fun ReThis.eval(script: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVAL".toArgument(), script.toArgument(), numKeys.toArgument(), *keys.toArgument()),
)

suspend fun ReThis.evalRo(script: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVAL_RO".toArgument(), script.toArgument(), numKeys.toArgument(), *keys.toArgument()),
)

suspend fun ReThis.evalSha(sha1: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVALSHA".toArgument(), sha1.toArgument(), numKeys.toArgument(), *keys.toArgument()),
)

suspend fun ReThis.evalShaRo(sha1: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVALSHA_RO".toArgument(), sha1.toArgument(), numKeys.toArgument(), *keys.toArgument()),
)

suspend fun ReThis.fcall(name: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("FCALL".toArgument(), name.toArgument(), numKeys.toArgument(), *keys.toArgument()),
)

suspend fun ReThis.fcallRo(name: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("FCALL_RO".toArgument(), name.toArgument(), numKeys.toArgument(), *keys.toArgument()),
)

suspend fun ReThis.functionDelete(name: String): Boolean = execute<String>(
    listOf("FUNCTION".toArgument(), "DELETE".toArgument(), name.toArgument()),
) == "OK"

suspend fun ReThis.functionDump(): ByteArray? = execute(
    listOf("FUNCTION".toArgument(), "DUMP".toArgument()),
    rawMarker = Unit,
).safeCast<RType.Raw>()?.value

suspend fun ReThis.functionFlush(): Boolean = execute<String>(
    listOf("FUNCTION".toArgument(), "FLUSH".toArgument()),
) == "OK"

suspend fun ReThis.functionKill(): Boolean = execute<String>(
    listOf("FUNCTION".toArgument(), "KILL".toArgument()),
) == "OK"

suspend fun ReThis.functionList(libraryName: String? = null, withCode: Boolean = false): List<RType> = execute(
    mutableListOf(
        "FUNCTION".toArgument(),
        "LIST".toArgument(),
    ).apply {
        libraryName?.let { writeArgument("LIBRARYNAME" to it) }
        if (withCode) writeArgument("WITHCODE")
    },
).unwrapList()

suspend fun ReThis.functionLoad(script: String): String? = execute<String>(
    listOf("FUNCTION".toArgument(), "LOAD".toArgument(), script.toArgument()),
)

suspend fun ReThis.functionRestore(
    serializedValue: ByteArray,
    option: FunctionRestoreOption? = null,
): Boolean = execute<String>(
    mutableListOf("FUNCTION".toArgument(), "RESTORE".toArgument(), serializedValue.toArgument()).writeArgument(option),
) == "OK"

suspend fun ReThis.functionStats(): Map<String, RType?>? = execute(
    listOf("FUNCTION".toArgument(), "STATS".toArgument()),
).unwrapRespIndMap()

suspend fun ReThis.scriptDebug(mode: String): String? = execute<String>(
    listOf("SCRIPT".toArgument(), "DEBUG".toArgument(), mode.toArgument()),
)

suspend fun ReThis.scriptExists(vararg shas: String): List<Boolean> = execute(
    listOf("SCRIPT".toArgument(), "EXISTS".toArgument(), *shas.toArgument()),
).unwrapList<Long>().map { it == 1L }

suspend fun ReThis.scriptFlush(): Boolean = execute<String>(
    listOf("SCRIPT".toArgument(), "FLUSH".toArgument()),
) == "OK"

suspend fun ReThis.scriptKill(): Boolean = execute<String>(
    listOf("SCRIPT".toArgument(), "KILL".toArgument()),
) == "OK"

suspend fun ReThis.scriptLoad(script: String): String? = execute<String>(
    listOf("SCRIPT".toArgument(), "LOAD".toArgument(), script.toArgument()),
)
