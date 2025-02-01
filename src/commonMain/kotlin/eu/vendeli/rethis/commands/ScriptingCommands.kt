package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.core.toArgument
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.options.FunctionRestoreOption
import eu.vendeli.rethis.utils.response.unwrapRespIndMap
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.writeArgument
import eu.vendeli.rethis.utils.execute

suspend fun ReThis.eval(script: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVAL".toArg(), script.toArg(), numKeys.toArg(), *keys.toArgument()),
)

suspend fun ReThis.evalRo(script: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVAL_RO".toArg(), script.toArg(), numKeys.toArg(), *keys.toArgument()),
)

suspend fun ReThis.evalSha(sha1: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVALSHA".toArg(), sha1.toArg(), numKeys.toArg(), *keys.toArgument()),
)

suspend fun ReThis.evalShaRo(sha1: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVALSHA_RO".toArg(), sha1.toArg(), numKeys.toArg(), *keys.toArgument()),
)

suspend fun ReThis.fcall(name: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("FCALL".toArg(), name.toArg(), numKeys.toArg(), *keys.toArgument()),
)

suspend fun ReThis.fcallRo(name: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("FCALL_RO".toArg(), name.toArg(), numKeys.toArg(), *keys.toArgument()),
)

suspend fun ReThis.functionDelete(name: String): Boolean = execute<String>(
    listOf("FUNCTION".toArg(), "DELETE".toArg(), name.toArg()),
) == "OK"

suspend fun ReThis.functionDump(): ByteArray? = execute(
    listOf("FUNCTION".toArg(), "DUMP".toArg()),
    rawMarker = Unit,
).safeCast<RType.Raw>()?.value

suspend fun ReThis.functionFlush(): Boolean = execute<String>(
    listOf("FUNCTION".toArg(), "FLUSH".toArg()),
) == "OK"

suspend fun ReThis.functionKill(): Boolean = execute<String>(
    listOf("FUNCTION".toArg(), "KILL".toArg()),
) == "OK"

suspend fun ReThis.functionList(libraryName: String? = null, withCode: Boolean = false): List<RType> = execute(
    mutableListOf(
        "FUNCTION".toArg(),
        "LIST".toArg(),
    ).apply {
        libraryName?.let { writeArgument("LIBRARYNAME" to it) }
        if (withCode) writeArgument("WITHCODE")
    },
).unwrapList()

suspend fun ReThis.functionLoad(script: String): String? = execute<String>(
    listOf("FUNCTION".toArg(), "LOAD".toArg(), script.toArg()),
)

suspend fun ReThis.functionRestore(
    serializedValue: ByteArray,
    option: FunctionRestoreOption? = null,
): Boolean = execute<String>(
    mutableListOf("FUNCTION".toArg(), "RESTORE".toArg(), serializedValue.toArg()).writeArgument(option),
) == "OK"

suspend fun ReThis.functionStats(): Map<String, RType?>? = execute(
    listOf("FUNCTION".toArg(), "STATS".toArg()),
).unwrapRespIndMap()

suspend fun ReThis.scriptDebug(mode: String): String? = execute<String>(
    listOf("SCRIPT".toArg(), "DEBUG".toArg(), mode.toArg()),
)

suspend fun ReThis.scriptExists(vararg shas: String): List<Boolean> = execute(
    listOf("SCRIPT".toArg(), "EXISTS".toArg(), *shas.toArgument()),
).unwrapList<Long>().map { it == 1L }

suspend fun ReThis.scriptFlush(): Boolean = execute<String>(
    listOf("SCRIPT".toArg(), "FLUSH".toArg()),
) == "OK"

suspend fun ReThis.scriptKill(): Boolean = execute<String>(
    listOf("SCRIPT".toArg(), "KILL".toArg()),
) == "OK"

suspend fun ReThis.scriptLoad(script: String): String? = execute<String>(
    listOf("SCRIPT".toArg(), "LOAD".toArg(), script.toArg()),
)
