package eu.vendeli.rethis.shared.request.server

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class DebugOption {
    @RedisOption.Token("OBJECT")
    class Object_(val key: String) : DebugOption()

    @RedisOption.Token("SLEEP")
    class Sleep(val seconds: Double) : DebugOption()

    @RedisOption.Token("SET-ACTIVE-EXPIRE")
    class SetActiveExpire(val enabled: Long) : DebugOption()

    @RedisOption.Token("SDSLEN")
    class Sdslen(val key: String) : DebugOption()

    @RedisOption.Token("QUICKLIST-PACKED-THRESHOLD")
    class QuicklistPackedThreshold(val bytes: Long) : DebugOption()

    @RedisOption.Token("STRINGMATCH-LEN")
    class StringmatchLen(val pattern: String, val target: String) : DebugOption()

    class Other(vararg val args: String) : DebugOption()
}
