package net.attilaszabo.redux

/**
 * A [Subscriber] that notifies the [PartialSubscriber] if the [T] sub-state changed.
 *
 * @param <S> The state type
 * @param <T> The sub-state type
 */
open class PartialSubscriber<in S, T>(private val subStateChangeListener: SubStateChangeListener<S, T>) : Subscriber<S> {

    // Members

    private var previousValue: T? = null

    // Subscriber

    override fun onStateChanged(state: S) {
        val subState = subStateChangeListener.getSubState(state)
        if (previousValue == null || subState != previousValue) {
            subStateChangeListener.onSubStateChanged(subState)
            previousValue = subState
        }
    }

    /**
     * A listener which will be called any time the [T] sub-state changed.
     *
     * @param <S> The state type
     * @param <T> The sub-state type
     */
    interface SubStateChangeListener<in S, T> {
        /**
         * Specify the sub-state you want to listen to.
         *
         * @param state The state
         * @return <T> The sub-state
         */
        fun getSubState(state: S): T

        /**
         * @param <T> The new sub-state
         */
        fun onSubStateChanged(subState: T)
    }
}
