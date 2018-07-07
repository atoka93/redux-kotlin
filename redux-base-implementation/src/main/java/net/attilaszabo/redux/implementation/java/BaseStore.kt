package net.attilaszabo.redux.implementation.java

import net.attilaszabo.redux.Action
import net.attilaszabo.redux.Reducer
import net.attilaszabo.redux.Store
import net.attilaszabo.redux.Subscriber
import net.attilaszabo.redux.Subscription
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Base implementation of the [Store] interface.
 *
 * @param <S> The state type
 * @param initialState The initial state
 * @param reducer The [Reducer]
 */
open class BaseStore<S>
protected constructor(initialState: S, reducer: Reducer<S>) : Store<S>(initialState, reducer) {

    // Members

    protected val isReducing = AtomicBoolean(false)
    protected val subscribers = ArrayList<Subscriber<S>>()

    // Store

    override fun subscribe(subscriber: Subscriber<S>): Subscription {
        subscribers.add(subscriber)
        return object : Subscription {
            override fun unsubscribe() {
                subscribers.remove(subscriber)
            }
        }
    }

    override fun replaceReducer(reducer: Reducer<S>) {
        this.reducer = reducer
    }

    override fun getState(): S = storedState

    override fun dispatch(action: Action) {
        if (isReducing.compareAndSet(false, true)) {
            try {
                val previousState = storedState
                storedState = reducer.reduce(storedState, action)

                if (previousState != storedState) {
                    subscribers.forEach { it.onStateChanged(storedState) }
                }
            } catch (e: Exception) {
                throw IllegalStateException("Dispatch exception: ${e.message}")
            } finally {
                isReducing.set(false)
            }
        }
    }

    // Creator

    class Creator<S> : Store.Creator<S> {

        override fun create(initialState: S, reducer: Reducer<S>) = BaseStore(initialState, reducer)
    }
}
