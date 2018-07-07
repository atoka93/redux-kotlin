package net.attilaszabo.redux.extensions.android.base

import net.attilaszabo.redux.Store
import net.attilaszabo.redux.Subscriber
import net.attilaszabo.redux.Subscription
import net.attilaszabo.redux.extensions.android.domain.navigation.NavigationActions
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationTag

abstract class BaseViewModel<S>(val navigationComponentTag: NavigationTag) {

    // Members

    protected lateinit var store: Store<S>
    private var subscription: Subscription? = null
    private var subscriber: Subscriber<S>? = null

    // Public Api

    open fun onCreate(store: Store<S>) {
        this.store = store
        subscriber = onCreateSubscriber()
    }

    open fun onResume() {
        subscriber?.let {
            subscription = store.subscribe(it)
            it.onStateChanged(store.getState())
        }
        store.dispatch(NavigationActions.NavigationComponentStarted(navigationComponentTag))
    }

    open fun onPause() {
        subscription?.unsubscribe()
    }

    open fun onBackPressed() {
    }

    open fun fragmentVisibilityChanged(isHidden: Boolean) {
        store.dispatch(NavigationActions.FragmentVisibilityChanged(navigationComponentTag, isHidden))
    }

    abstract fun onCreateSubscriber(): Subscriber<S>
}
