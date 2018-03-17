package net.attilaszabo.redux.rx

import io.reactivex.Observable
import net.attilaszabo.redux.Store

/**
 * A reactive listener which will be called any time the [T] sub-state changed.
 *
 * @param <S> The state type
 * @param <T> The sub-state type
 */
abstract class RxPartialSubscriber<S, T>(store: Store<S>) : RxSubscriber<S>(store) {

    /**
     * Sub-state observable.
     *
     * @return <T> The sub-state
     */
    fun subState(): Observable<T> = super.state()
            .map { getSubState(it) }
            .distinctUntilChanged()

    /**
     * Specify the sub-state you want to listen to.
     *
     * @param state The state
     * @return <T> The sub-state
     */
    abstract fun getSubState(state: S): T
}
