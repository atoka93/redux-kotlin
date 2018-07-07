package net.attilaszabo.redux

import net.attilaszabo.redux.enhancers.Enhancer

/**
 * An object that holds the application's state tree.
 * The store has the following responsibilities:
 *  * Hold the application state
 *  * Allow access to the state
 *  * Allow the state to be updated
 *  * Register listeners
 *  * Handle unregistering listeners
 *
 * @param <S> The state type
 * @param storedState The state
 * @param reducer The [Reducer]
 */
abstract class Store<S>
protected constructor(protected var storedState: S, protected var reducer: Reducer<S>) : Dispatcher {

    /**
     * Adds a change listener.
     *
     * @param subscriber The subscriber
     * @return A [Subscription]
     */
    abstract fun subscribe(subscriber: Subscriber<S>): Subscription

    /**
     * Returns the current state tree of the application.
     *
     * @return The current state
     */
    abstract fun getState(): S

    /**
     * Replaces the currently used reducer.
     *
     * @param reducer The [Reducer]
     */
    abstract fun replaceReducer(reducer: Reducer<S>)

    /**
     * An interface that creates a store.
     */
    interface Creator<S> {

        /**
         * @param initialState The initial state
         * @param reducer The [Reducer]
         * @return The store
         */
        fun create(initialState: S, reducer: Reducer<S>): Store<S>
    }

    companion object {

        /**
         * Create a [Store] based on a [Creator] while also enhancing it with the given enhancers.
         *
         * @param <S> The state type
         * @param storeCreator A [Creator]
         * @param initialState The initial state
         * @param reducer The [Reducer]
         * @param [enhancers] A list of enhancers
         * @return The store
         */
        fun <S> createStore(storeCreator: Creator<S>, initialState: S, reducer: Reducer<S>, vararg enhancers: Enhancer<S>): Store<S> {
            var creator: Creator<S> = storeCreator
            creator = enhancers.fold(creator) { enhancerCreator, enhancer ->
                enhancer.enhance(enhancerCreator)
            }
            return creator.create(initialState, reducer)
        }
    }
}
