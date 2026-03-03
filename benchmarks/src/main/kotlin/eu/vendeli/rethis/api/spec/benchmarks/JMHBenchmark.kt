package eu.vendeli.rethis.api.spec.benchmarks

import com.redis.testcontainers.RedisContainer
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.openjdk.jmh.annotations.*
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private const val THREADS_COUNT = 4

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Threads(THREADS_COUNT)
@Fork(1)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
abstract class JMHBenchmark {
    protected val benchScope = CoroutineScope(Dispatchers.IO + CoroutineName(this::class.simpleName!!))
    protected val random = Random.Default
    protected val opsCount = THREADS_COUNT * 2

    companion object {
        val redis = RedisContainer(
            DockerImageName.parse("redis:7.4.0"),
        )
    }
}
