package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.FunctionRestoreCommandCodec
import eu.vendeli.rethis.shared.request.scripting.FunctionRestoreOption
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.functionRestore(serializedValue: ByteArray, policy: FunctionRestoreOption? = null): Boolean {
    val request = if (cfg.withSlots) {
        FunctionRestoreCommandCodec.encodeWithSlot(
            charset = cfg.charset,
            serializedValue = serializedValue,
            policy = policy,
        )
    } else {
        FunctionRestoreCommandCodec.encode(charset = cfg.charset, serializedValue = serializedValue, policy = policy)
    }
    return FunctionRestoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
