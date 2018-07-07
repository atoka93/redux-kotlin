package net.attilaszabo.redux.extensions.android

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ComponentCallbacks2
import android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND
import android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE
import android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE
import android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import net.attilaszabo.redux.Reducer
import net.attilaszabo.redux.Store
import net.attilaszabo.redux.enhancers.Enhancer
import net.attilaszabo.redux.implementation.java.AsyncStore

class ReduxActivityLifecycleCallbacks<S>(
    private val statePersistenceListener: StatePersistenceListener<S>,
    private val initialState: S,
    private val reducer: Reducer<S>,
    private val enhancer: Enhancer<S>
) : ActivityLifecycleCallbacks, ComponentCallbacks2 {

    // Members

    private lateinit var store: Store<S>
    private var firstActivityCreated: Boolean = false

    // ActivityLifecycleCallbacks

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        if (firstActivityCreated) {
            return
        }
        firstActivityCreated = true

        store = AsyncStore.create(
            mainThreadExecutor = ::executor,
            initialState = statePersistenceListener.restoreState(savedInstanceState) ?: initialState,
            reducer = reducer,
            enhancers = *arrayOf(enhancer)
        )
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        statePersistenceListener.saveState(outState, statePersistenceListener.removeTransientState(store.getState()))
    }

    // ComponentCallbacks2

    override fun onLowMemory() {
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
    }

    override fun onTrimMemory(level: Int) {
        if (level in listOf(TRIM_MEMORY_UI_HIDDEN, TRIM_MEMORY_BACKGROUND, TRIM_MEMORY_MODERATE, TRIM_MEMORY_COMPLETE)) {
            statePersistenceListener.saveStateToFile(store.getState())
        }
    }

    // Public Api

    fun getStore(): Store<S> = store

    // Private Api

    private fun executor(runnable: Runnable) {
        Handler(Looper.getMainLooper()).post(runnable)
    }

    interface StatePersistenceListener<S> {

        fun removeTransientState(state: S): S {
            return state
        }

        fun saveState(outInstanceState: Bundle?, state: S)

        fun saveStateToFile(state: S)

        fun restoreState(savedInstanceState: Bundle?): S?
    }
}
