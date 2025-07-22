package eu.vendeli.rethis.codecs.connection

import eu.vendeli.rethis.api.spec.common.decoders.general.IntegerDecoder
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions.Address
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions.Id
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions.LAddr
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions.MaxAge
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions.SkipMe
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions.SkipMe.No
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions.SkipMe.Yes
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions.User
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType.Master
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType.Normal
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType.PubSub
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType.Replica
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType.Slave
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.writeInstantArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ClientKillCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nCLIENT\r\n$4\r\nKILL\r\n")
    }

    public suspend fun encode(charset: Charset, vararg filter: ClientKillOptions): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        filter.forEach { it0 ->
            when (it0) {
                is ClientKillOptions.Address ->  {
                    size += 1
                    buffer.writeStringArg("ADDR", charset)
                    size += 1
                    buffer.writeStringArg(it0.ipPort, charset, )
                }
                is ClientKillOptions.Id ->  {
                    size += 1
                    buffer.writeStringArg("ID", charset)
                    size += 1
                    buffer.writeLongArg(it0.clientId, charset, )
                }
                is ClientKillOptions.LAddr ->  {
                    size += 1
                    buffer.writeStringArg("LADDR", charset)
                    size += 1
                    buffer.writeStringArg(it0.ipPort, charset, )
                }
                is ClientKillOptions.MaxAge ->  {
                    size += 1
                    buffer.writeStringArg("MAXAGE", charset)
                    size += 1
                    buffer.writeInstantArg(it0.instant, charset, TimeUnit.MILLISECONDS)
                }
                is ClientKillOptions.SkipMe ->  {
                    size += 1
                    buffer.writeStringArg("SKIPME", charset)
                    when (it0) {
                        is ClientKillOptions.SkipMe.Yes ->  {
                            size += 1
                            buffer.writeStringArg("YES", charset)
                        }
                        is ClientKillOptions.SkipMe.No ->  {
                            size += 1
                            buffer.writeStringArg("NO", charset)
                        }
                    }
                }
                is ClientKillOptions.User ->  {
                    size += 1
                    buffer.writeStringArg("USER", charset)
                    size += 1
                    buffer.writeStringArg(it0.username, charset, )
                }
                is ClientType ->  {
                    size += 1
                    buffer.writeStringArg("TYPE", charset)
                    when (it0) {
                        is ClientType.Normal ->  {
                            size += 1
                            buffer.writeStringArg("NORMAL", charset)
                        }
                        is ClientType.Master ->  {
                            size += 1
                            buffer.writeStringArg("MASTER", charset)
                        }
                        is ClientType.Slave ->  {
                            size += 1
                            buffer.writeStringArg("SLAVE", charset)
                        }
                        is ClientType.Replica ->  {
                            size += 1
                            buffer.writeStringArg("REPLICA", charset)
                        }
                        is ClientType.PubSub ->  {
                            size += 1
                            buffer.writeStringArg("PUBSUB", charset)
                        }
                    }
                }
            }
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, vararg filter: ClientKillOptions): CommandRequest = encode(charset, filter = filter)

    public suspend fun decode(input: Buffer, charset: Charset): Long {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
