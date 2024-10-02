package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.options.FunctionRestoreOption
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.unwrapRespIndMap

suspend fun ReThis.eval(script: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVAL", script, numKeys, *keys),
)

suspend fun ReThis.evalRo(script: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVAL_RO", script, numKeys, *keys),
)

suspend fun ReThis.evalSha(sha1: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVALSHA", sha1, numKeys, *keys),
)

suspend fun ReThis.evalShaRo(sha1: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("EVALSHA_RO", sha1, numKeys, *keys),
)

suspend fun ReThis.fcall(name: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("FCALL", name, numKeys, *keys),
)

suspend fun ReThis.fcallRo(name: String, numKeys: Long, vararg keys: String): RType = execute(
    listOf("FCALL_RO", name, numKeys, *keys),
)

suspend fun ReThis.functionDelete(name: String): String? = execute(
    listOf("FUNCTION", "DELETE", name),
).unwrap()

suspend fun ReThis.functionDump(): ByteArray? = execute(
    listOf("FUNCTION", "DUMP"),
    rawResponse = true,
).safeCast<RType.Raw>()?.value

suspend fun ReThis.functionFlush(): String? = execute(
    listOf("FUNCTION", "FLUSH"),
).unwrap()

suspend fun ReThis.functionKill(): String? = execute(
    listOf("FUNCTION", "KILL"),
).unwrap()

suspend fun ReThis.functionList(libraryName: String? = null, withCode: Boolean = false): List<RType> = execute(
    listOfNotNull(
        "FUNCTION",
        "LIST",
        libraryName?.let { "LIBRARYNAME" to it },
        withCode.takeIf { it }?.let { "WITHCODE" },
    ),
).unwrapList()

suspend fun ReThis.functionLoad(script: String): String? = execute(
    listOf("FUNCTION", "LOAD", script),
).unwrap()

suspend fun ReThis.functionRestore(
    serializedValue: ByteArray,
    option: FunctionRestoreOption? = null,
): String? = execute(
    listOfNotNull("FUNCTION", "RESTORE", serializedValue, option),
).unwrap()

suspend fun ReThis.functionStats(): Map<String, RType?>? = execute(
    listOf("FUNCTION", "STATS"),
).unwrapRespIndMap()

suspend fun ReThis.scriptDebug(mode: String): String? = execute(
    listOf("SCRIPT", "DEBUG", mode),
).unwrap()

suspend fun ReThis.scriptExists(vararg shas: String): List<Boolean> = execute(
    listOf("SCRIPT", "EXISTS", *shas),
).unwrapList<Long>().map { it == 1L }

suspend fun ReThis.scriptFlush(): String? = execute(
    listOf("SCRIPT", "FLUSH"),
).unwrap()

suspend fun ReThis.scriptKill(): String? = execute(
    listOf("SCRIPT", "KILL"),
).unwrap()

suspend fun ReThis.scriptLoad(script: String): String? = execute(
    listOf("SCRIPT", "LOAD", script),
).unwrap()
