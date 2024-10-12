package eu.vendeli.rethis.types.core

interface SubscriptionEventHandler {
    suspend fun onSubscribe(id: String, subscribedChannels: Long)
    suspend fun onUnsubscribe(id: String, subscribedChannels: Long)
    suspend fun onException(id: String, ex: Exception)
}
