package eu.vendeli.rethis.shared.request.sentinel

import eu.vendeli.rethis.shared.annotations.RedisOption

enum class SentinelSimulateFailureMode(private val literal: String) {
    @RedisOption.Token("crash-after-election")
    CRASH_AFTER_ELECTION("crash-after-election"),

    @RedisOption.Token("crash-after-promotion")
    CRASH_AFTER_PROMOTION("crash-after-promotion"),

    @RedisOption.Token("help")
    HELP("help");

    override fun toString(): String = literal
}
