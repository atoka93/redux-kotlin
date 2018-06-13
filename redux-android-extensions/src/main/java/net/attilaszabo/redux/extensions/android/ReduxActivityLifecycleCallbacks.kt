package net.attilaszabo.redux.extensions.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import net.attilaszabo.redux.Reducer
import net.attilaszabo.redux.Store
import net.attilaszabo.redux.enhancers.Enhancer
import net.attilaszabo.redux.implementation.java.AsyncStore

class ReduxActivityLifecycleCallbacks<S>(
        private val mStatePersistenceListener: StatePersistenceListener<S>,
        private val mInitialState: S,
        private val mReducer: Reducer<S>,
        private val mEnhancer: Enhancer<S>
) : Application.ActivityLifecycleCallbacks {

    // Members

    private lateinit var mStore: Store<S>
    private var mFirstActivityCreated: Boolean = false

    // ActivityLifecycleCallbacks

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        if (mFirstActivityCreated) {
            return
        }
        mFirstActivityCreated = true

        mStore = AsyncStore.create(
                mainThreadExecutor = ::executor,
                initialState = mStatePersistenceListener.restoreState(savedInstanceState)
                        ?: mInitialState,
                reducer = mReducer,
                enhancers = *arrayOf(mEnhancer)
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
        mStatePersistenceListener.saveState(outState, mStatePersistenceListener.removeTransientState(mStore.getState()))
    }

    // Public Api

    fun getStore(): Store<S> = mStore

    // Private Api

    private fun executor(runnable: Runnable) {
        Handler(Looper.getMainLooper()).post(runnable)
    }

    interface StatePersistenceListener<S> {

        fun removeTransientState(state: S): S {
            return state
        }

        fun saveState(outInstanceState: Bundle?, state: S)

        fun restoreState(savedInstanceState: Bundle?): S?
    }
}
