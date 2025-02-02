package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.utils.writeArgument

class XPendingOption(
    start: String,
    end: String,
    count: Long,
    consumer: String? = null,
    minIdleTime: Long? = null,
) : VaryingArgument {
    override val data: List<Argument> = mutableListOf<Argument>().writeArgument(
        minIdleTime?.let { "MINIDLETIME" to it },
        start,
        end,
        count,
        consumer,
    )
}
