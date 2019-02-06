package net.attilaszabo.redux.extensions.android.base

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.attilaszabo.redux.Store
import net.attilaszabo.redux.extensions.android.ReduxActivityLifecycleCallbacks
import net.attilaszabo.redux.extensions.android.ReduxActivityLifecycleCallbacks.StatePersistenceListener
import net.attilaszabo.redux.extensions.android.navigation.NavigationHandler
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationTag

abstract class BaseApplication<S> : Application(), StatePersistenceListener<S> {

    // Members

    protected lateinit var navigationHandler: NavigationHandler
    protected lateinit var activityLifecycleCallbacks: ReduxActivityLifecycleCallbacks<S>

    // Public Api

    fun getStore(): Store<S> = activityLifecycleCallbacks.getStore()

    fun setActiveActivity(identifier: NavigationTag, activity: AppCompatActivity?) {
        navigationHandler.setActivity(activity)
        setFragmentManager(identifier, activity?.supportFragmentManager)
    }

    fun setFragmentManager(identifier: NavigationTag, fragmentManager: androidx.fragment.app.FragmentManager?) {
        navigationHandler.addFragmentManager(identifier, fragmentManager)
    }

    fun setBundle(identifier: NavigationTag, bundle: Bundle) {
        navigationHandler.addBundle(identifier, bundle)
    }
}
