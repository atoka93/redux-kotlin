package net.attilaszabo.redux

/**
 * A listener which will be called any time the state is changed.
 *
 * @param <S> The state type
 */
interface Subscriber<in S> {

    /**
     * @param state The current application state
     */
    fun onStateChanged(state: S)
}
