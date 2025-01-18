package eu.vendeli.rethis.tests

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.del
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class MultiprocessingTest : ReThisTestCtx() {
    @Test
    suspend fun `multiprocessing test`() = withContext(Dispatchers.IO) {
        (1..90)
            .map {
                async {
                    client.del("key$it")
                    println("deleted key$it")
                }
            }.awaitAll()
    }
}
