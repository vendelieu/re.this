package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.generic.MigrateKey
import eu.vendeli.rethis.shared.request.generic.MigrateOption
import eu.vendeli.rethis.codecs.generic.MigrateCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.time.Duration

public suspend fun ReThis.migrate(
    host: String,
    port: Long,
    keySelector: MigrateKey,
    destinationDb: Long,
    timeout: Duration,
    vararg option: MigrateOption,
): String {
    val request = if(cfg.withSlots) {
        MigrateCommandCodec.encodeWithSlot(charset = cfg.charset, host = host, port = port, keySelector = keySelector, destinationDb = destinationDb, timeout = timeout, option = option)
    } else {
        MigrateCommandCodec.encode(charset = cfg.charset, host = host, port = port, keySelector = keySelector, destinationDb = destinationDb, timeout = timeout, option = option)
    }
    return MigrateCommandCodec.decode(topology.handle(request), cfg.charset)
}
