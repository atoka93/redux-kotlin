package net.attilaszabo.redux.rx

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import net.attilaszabo.redux.Store
import net.attilaszabo.redux.Subscriber
import net.attilaszabo.redux.Subscription

/**
 * A reactive listener which will be called any time the state changed.
 *
 * @param <S> The state type
 */
open class RxSubscriber<S>(store: Store<S>) : Subscriber<S> {

    // Members

    private val mState: PublishRelay<S> = PublishRelay.create()
    private val mSubscription: Subscription = store.subscribe(this)

    // Subscriber

    override fun onStateChanged(state: S) {
        this@RxSubscriber.mState.accept(state)
    }

    // Public Api

    /**
     * State observable.
     *
     * @return <T> The state
     */
    fun state(): Observable<S> = mState
            .doOnDispose { mSubscription.unsubscribe() }
}
