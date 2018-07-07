package net.attilaszabo.redux.extensions.android.domain.navigation

import net.attilaszabo.redux.Action
import net.attilaszabo.redux.Reducer
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationState

open class NavigationReducer : Reducer<NavigationState> {

    // Reducer

    override fun reduce(state: NavigationState, action: Action): NavigationState = when (action) {
        is NavigationActions.UpdateTo -> {
            action.state
        }
        else -> state
    }
}
