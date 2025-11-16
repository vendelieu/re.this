package eu.vendeli.rethis.core

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.types.common.Subscription
import eu.vendeli.rethis.types.common.SubscriptionType
import eu.vendeli.rethis.types.interfaces.SubscriptionHandler
import org.junit.jupiter.api.assertDoesNotThrow

class SubscriptionManagerTest : ReThisTestCtx() {
    @Test
    fun `Given pattern subscriptions, When unsubscribing all, Then no exception is thrown`() {
        // Given
        val manager = SubscriptionManager()
        manager.registerSubscription(
            "1",
            Subscription(type = SubscriptionType.PATTERN, handler = SubscriptionHandler({ a, b -> })),
        )
        manager.registerSubscription(
            "2",
            Subscription(type = SubscriptionType.PATTERN, handler = SubscriptionHandler({ a, b -> })),
        )

        // Then
        assertDoesNotThrow {
            // When
            manager.unsubscribeAll()
        }
    }
}
