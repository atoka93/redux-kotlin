package net.attilaszabo.redux.enhancers

import net.attilaszabo.redux.Action
import net.attilaszabo.redux.Dispatcher
import net.attilaszabo.redux.Reducer
import net.attilaszabo.redux.Store
import net.attilaszabo.redux.Store.Creator
import net.attilaszabo.redux.Subscriber
import net.attilaszabo.redux.Subscription

/**
 * A middleware composes a [Dispatcher] to return a new dispatch function.
 *
 * @param <S> The state type
 */
interface Middleware<in S> {

    /**
     * Intercepts an action to perform something, next always has to be called.
     *
     * @param state The current state
     * @param action An [Action]
     * @param next The next dispatcher in the chain
     * @param dispatcher The original dispatcher to dispatch additional actions
     */
    fun dispatch(state: () -> S, action: Action, next: Dispatcher, dispatcher: Dispatcher)

    companion object {

        /**
         * An [Enhancer] that combines middlewares.
         *
         * @param <S> The state type
         * @param [middlewares] A list of middlewares
         * @return An [Enhancer]
         */
        fun <S> applyMiddlewares(vararg middlewares: Middleware<S>): Enhancer<S> = object : Enhancer<S> {
            override fun enhance(next: Creator<S>): Creator<S> = object : Creator<S> {
                override fun create(initialState: S, reducer: Reducer<S>): Store<S> = object : Store<S>(initialState, reducer) {

                    // Members

                    private val mStoreInstance: Store<S> = this
                    private val mStore = next.create(initialState, reducer)

                    // Store

                    override fun subscribe(subscriber: Subscriber<S>): Subscription = mStore.subscribe(subscriber)

                    override fun getState(): S = mStore.getState()

                    override fun replaceReducer(reducer: Reducer<S>) = mStore.replaceReducer(reducer)

                    override fun dispatch(action: Action) {
                        val combinedDispatcher = middlewares.foldRight(mStore as Dispatcher) { middleware, next ->
                            object : Dispatcher {
                                override fun dispatch(action: Action) =
                                        middleware.dispatch(mStoreInstance::getState, action, next, mStoreInstance)
                            }
                        }
                        combinedDispatcher.dispatch(action)
                    }
                }
            }
        }
    }
}
