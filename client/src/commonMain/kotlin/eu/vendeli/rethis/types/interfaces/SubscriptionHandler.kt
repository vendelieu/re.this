package eu.vendeli.rethis.types.interfaces

import eu.vendeli.rethis.ReThis

fun interface SubscriptionHandler {
    suspend fun onMessage(client: ReThis, message: String)
}
