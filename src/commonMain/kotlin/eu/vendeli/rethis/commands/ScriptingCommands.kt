package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.options.FunctionRestoreOption
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.unwrapRespIndMap
import eu.vendeli.rethis.utils.writeArg

suspend fun ReThis.eval(script: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVAL".toArg(), script.toArg(), numKeys.toArg(), *keys.toArg()),
)

suspend fun ReThis.evalRo(script: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVAL_RO".toArg(), script.toArg(), numKeys.toArg(), *keys.toArg()),
)

suspend fun ReThis.evalSha(sha1: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVALSHA".toArg(), sha1.toArg(), numKeys.toArg(), *keys.toArg()),
)

suspend fun ReThis.evalShaRo(sha1: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVALSHA_RO".toArg(), sha1.toArg(), numKeys.toArg(), *keys.toArg()),
)

suspend fun ReThis.fcall(name: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("FCALL".toArg(), name.toArg(), numKeys.toArg(), *keys.toArg()),
)

suspend fun ReThis.fcallRo(name: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("FCALL_RO".toArg(), name.toArg(), numKeys.toArg(), *keys.toArg()),
)

suspend fun ReThis.functionDelete(name: String): String? = execute(
    listOf("FUNCTION".toArg(), "DELETE".toArg(), name.toArg()),
).unwrap()

suspend fun ReThis.functionDump(): ByteArray? = execute(
    listOf("FUNCTION".toArg(), "DUMP".toArg()),
    rawResponse = true,
).safeCast<RType.Raw>()?.value

suspend fun ReThis.functionFlush(): String? = execute(
    listOf("FUNCTION".toArg(), "FLUSH".toArg()),
).unwrap()

suspend fun ReThis.functionKill(): String? = execute(
    listOf("FUNCTION".toArg(), "KILL".toArg()),
).unwrap()

suspend fun ReThis.functionList(libraryName: String? = null, withCode: Boolean = false): List<RType> = execute(
    mutableListOf(
        "FUNCTION".toArg(),
        "LIST".toArg(),
    ).apply {
        libraryName?.let { writeArg("LIBRARYNAME" to it) }
        if (withCode) writeArg("WITHCODE")
    },
).unwrapList()

suspend fun ReThis.functionLoad(script: String): String? = execute(
    listOf("FUNCTION".toArg(), "LOAD".toArg(), script.toArg()),
).unwrap()

suspend fun ReThis.functionRestore(
    serializedValue: ByteArray,
    option: FunctionRestoreOption? = null,
): String? = execute(
    mutableListOf("FUNCTION".toArg(), "RESTORE".toArg(), serializedValue.toArg()).writeArg(option),
).unwrap()

suspend fun ReThis.functionStats(): Map<String, RType?>? = execute(
    listOf("FUNCTION".toArg(), "STATS".toArg()),
).unwrapRespIndMap()

suspend fun ReThis.scriptDebug(mode: String): String? = execute(
    listOf("SCRIPT".toArg(), "DEBUG".toArg(), mode.toArg()),
).unwrap()

suspend fun ReThis.scriptExists(vararg shas: String): List<Boolean> = execute(
    listOf("SCRIPT".toArg(), "EXISTS".toArg(), *shas.toArg()),
).unwrapList<Long>().map { it == 1L }

suspend fun ReThis.scriptFlush(): String? = execute(
    listOf("SCRIPT".toArg(), "FLUSH".toArg()),
).unwrap()

suspend fun ReThis.scriptKill(): String? = execute(
    listOf("SCRIPT".toArg(), "KILL".toArg()),
).unwrap()

suspend fun ReThis.scriptLoad(script: String): String? = execute(
    listOf("SCRIPT".toArg(), "LOAD".toArg(), script.toArg()),
).unwrap()
