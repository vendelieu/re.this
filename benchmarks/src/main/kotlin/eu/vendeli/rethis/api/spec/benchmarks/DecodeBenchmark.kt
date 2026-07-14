package eu.vendeli.rethis.api.spec.benchmarks

import eu.vendeli.rethis.shared.decoders.aggregate.MapStringDecoder
import eu.vendeli.rethis.shared.utils.readCompleteResponseInto
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
 * Pure in-memory RESP pipeline benchmark (no server, no sockets):
 * an HGETALL-shaped reply of 1000 fields including one 30 KB value,
 * measured separately for the framing pass and the decode pass.
 */
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
class DecodeBenchmark {
    private lateinit var wireReply: ByteArray
    private val framedReply = Buffer()

    @Setup(Level.Trial)
    fun setup() {
        wireReply = buildString {
            append("*2000\r\n")
            repeat(1000) { i ->
                val key = "field$i"
                val value = if (i == 500) "x".repeat(30_000) else "value-$i-payload"
                append("\$${key.length}\r\n$key\r\n")
                append("\$${value.length}\r\n$value\r\n")
            }
        }.encodeToByteArray()

        runBlocking { ByteReadChannel(wireReply).readCompleteResponseInto(framedReply) }
    }

    @Benchmark
    fun frameHGetAllReply(bh: Blackhole) = runBlocking {
        val frame = Buffer()
        ByteReadChannel(wireReply).readCompleteResponseInto(frame)
        bh.consume(frame)
    }

    @Benchmark
    fun decodeHGetAllReply(bh: Blackhole) {
        bh.consume(MapStringDecoder.decodeNullable(framedReply.copy(), Charsets.UTF_8))
    }
}
