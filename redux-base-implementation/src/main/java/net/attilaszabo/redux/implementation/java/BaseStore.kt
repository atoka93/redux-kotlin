package net.attilaszabo.redux.implementation.java

import net.attilaszabo.redux.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Base implementation of the [Store] interface.
 *
 * @param <S> The state type
 * @param initialState The initial state
 * @param reducer The [Reducer]
 */
open class BaseStore<S>
protected constructor(initialState: S, reducer: Reducer<S>)
    : Store<S>(initialState, reducer) {

    // Members

    protected val mIsReducing = AtomicBoolean(false)
    protected val mSubscribers = ArrayList<Subscriber<S>>()

    // Store

    override fun subscribe(subscriber: Subscriber<S>): Subscription {
        mSubscribers.add(subscriber)
        return object : Subscription {
            override fun unsubscribe() {
                mSubscribers.remove(subscriber)
            }
        }
    }

    override fun replaceReducer(reducer: Reducer<S>) {
        mReducer = reducer
    }

    override fun getState(): S = mState

    override fun dispatch(action: Action) {
        if (mIsReducing.compareAndSet(false, true)) {
            try {
                val previousState = mState
                mState = mReducer.reduce(mState, action)

                if (previousState != mState) {
                    mSubscribers.forEach { it.onStateChanged(mState) }
                }
            } catch (e: Exception) {
                throw IllegalStateException("Dispatch exception: ${e.message}")
            } finally {
                mIsReducing.set(false)
            }
        }
    }

    // Creator

    class Creator<S> : Store.Creator<S> {

        override fun create(initialState: S, reducer: Reducer<S>) = BaseStore(initialState, reducer)
    }
}
