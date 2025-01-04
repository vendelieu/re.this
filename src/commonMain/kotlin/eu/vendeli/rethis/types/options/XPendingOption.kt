package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.utils.writeArg

class XPendingOption(
    start: String,
    end: String,
    count: Long,
    consumer: String? = null,
    minIdleTime: Long? = null,
) : VaryingArgument {
    override val data: List<Argument> = mutableListOf<Argument>().writeArg(
        minIdleTime?.let { "MINIDLETIME" to it },
        start,
        end,
        count,
        consumer,
    )
}
