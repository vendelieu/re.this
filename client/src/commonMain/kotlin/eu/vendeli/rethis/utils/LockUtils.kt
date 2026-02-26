package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.scripting.eval
import eu.vendeli.rethis.shared.utils.unwrap

internal object LockUtils {
    const val ACQUIRE_LUA = """
            local vals = redis.call('HMGET', KEYS[1], 'owner', 'count')
            if not vals[1] then
                redis.call('HSET', KEYS[1], 'owner', ARGV[1], 'count', 1)
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
                return 1
            end
            if vals[1] == ARGV[1] then
                redis.call('HSET', KEYS[1], 'count', tonumber(vals[2]) + 1)
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
                return 1
            end
            return 0
        """

    const val RELEASE_LUA = """
            local vals = redis.call('HMGET', KEYS[1], 'owner', 'count')
            if not vals[1] then return 0 end
            if vals[1] ~= ARGV[1] then return -1 end
            local count = tonumber(vals[2]) - 1
            if count > 0 then
                redis.call('HSET', KEYS[1], 'count', count)
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
                return 1
            else
                redis.call('DEL', KEYS[1])
                redis.call('PUBLISH', ARGV[3], 'released')
                return 1
            end
        """

    const val REFRESH_LUA = """
            if redis.call('HGET', KEYS[1], 'owner') == ARGV[1] then
                redis.call('PEXPIRE', KEYS[1], ARGV[2])
                return 1
            end
            return 0
        """
}

internal suspend fun ReThis.evalAsInt(
    script: String,
    keys: Array<String>,
    args: List<String>,
): Int {
    return eval(script = script, key = keys, arg = args).unwrap<Long?>()?.toInt() ?: 0
}
