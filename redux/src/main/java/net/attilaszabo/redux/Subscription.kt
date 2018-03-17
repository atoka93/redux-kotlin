package net.attilaszabo.redux

/**
 * A reference to the [Subscriber] to allow unsubscription.
 */
interface Subscription {

    /**
     * Unsubscribe the [Subscriber] from the [Store].
     */
    fun unsubscribe()
}
