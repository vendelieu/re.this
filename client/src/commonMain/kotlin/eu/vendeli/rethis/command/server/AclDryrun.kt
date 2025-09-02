package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclDryRunCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.aclDryRun(
    username: String,
    command: String,
    vararg arg: String,
): String {
    val request = if(cfg.withSlots) {
        AclDryRunCommandCodec.encodeWithSlot(charset = cfg.charset, username = username, command = command, arg = arg)
    } else {
        AclDryRunCommandCodec.encode(charset = cfg.charset, username = username, command = command, arg = arg)
    }
    return AclDryRunCommandCodec.decode(topology.handle(request), cfg.charset)
}
