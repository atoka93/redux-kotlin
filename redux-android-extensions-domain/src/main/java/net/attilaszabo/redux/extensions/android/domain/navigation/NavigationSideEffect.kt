package net.attilaszabo.redux.extensions.android.domain.navigation

import net.attilaszabo.redux.Action
import net.attilaszabo.redux.Dispatcher
import net.attilaszabo.redux.enhancers.SideEffect
import net.attilaszabo.redux.extensions.android.state.navigation.ActivityState
import net.attilaszabo.redux.extensions.android.state.navigation.FragmentState
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationComponent
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationContainerId
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationState
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationTag
import java.util.LinkedList

open class NavigationSideEffect<S>(
    private val subState: SubStateSupplier<S, NavigationState>,
    private val navigationHandler: NavigationHandler
) : SideEffect<S>() {

    // Members

    private var upcomingActions: LinkedList<NavigationActions> = LinkedList()

    // SideEffect

    override fun dispatch(state: () -> S, action: Action, dispatcher: Dispatcher) {
        if (action !is NavigationActions) return
        val navigationState = subState.getSubState(state()).copy()
        handleNavigationAction(navigationState, action, dispatcher)
        when (action) {
            is NavigationActions.ShowDialogFragment -> {
                if (action.hideKeyboard) {
                    navigationHandler.hideKeyboard()
                }
                navigationHandler.showDialogFragment(action.parentTag, action.targetTag, action.requestCode, action.tag)
            }
            is NavigationActions.DispatchOnBackPressedToVisibleFragments -> {
                getVisibleChildren(navigationState, action.parentTag)?.forEach {
                    (it.value as? FragmentState)?.let { fragmentState ->
                        navigationHandler.callOnBackPressed(fragmentState.parentTag, fragmentState.tag)
                    }
                }
            }
            is NavigationActions.CloseFragment -> {
                val navigationComponent = navigationState.navigationComponents[action.tag]
                if (navigationComponent != null && navigationComponent is FragmentState) {
                    removeChildrenRecursively(navigationState, navigationComponent.tag, dispatcher)
                    dispatcher.dispatch(NavigationActions.UpdateTo(navigationState))
                    getVisibleChildrenWithContainer(navigationState, navigationComponent.parentTag, navigationComponent.containerId)?.forEach {
                        (it.value as? FragmentState)?.apply {
                            navigationHandler.openFragment(this.parentTag, this.containerId, this.tag)
                        }
                    }
                }
            }
            is NavigationActions.FinishActivity -> {
                val parentTag = navigationState.navigationComponents[navigationState.activeActivityTag]?.parentTag
                navigationState.activeActivityTag?.apply {
                    removeChildrenRecursively(navigationState, this, dispatcher)
                    dispatcher.dispatch(NavigationActions.UpdateTo(NavigationState(parentTag, navigationState.navigationComponents)))
                }
                if (parentTag != null) {
                    getChildrenTagsRecursively(navigationState, parentTag)
                        .filter {
                            val navigationComponent = navigationState.navigationComponents[it]
                            it != parentTag && navigationComponent?.isVisible == true && navigationComponent is FragmentState
                        }
                        .reversed()
                        .forEach {
                            (navigationState.navigationComponents[it] as? FragmentState)?.apply {
                                upcomingActions.add(NavigationActions.OpenFragment(parentTag = this.parentTag, containerId = this.containerId, tag = this.tag))
                            }
                        }
                    navigationHandler.startActivity(
                        tag = parentTag,
                        requestCode = (navigationState.navigationComponents[navigationState.activeActivityTag] as? ActivityState)?.requestCode
                    )
                } else {
                    navigationHandler.finishActivity()
                }
            }
            is NavigationActions.HideKeyboard -> {
                navigationHandler.hideKeyboard()
            }
        }
    }

    private fun handleNavigationAction(state: NavigationState, action: NavigationActions, dispatcher: Dispatcher) {
        when (action) {
            is NavigationActions.StartActivity -> {
                var parentTag: NavigationTag? = state.activeActivityTag
                if (action.replaceCurrentActivity) {
                    state.navigationComponents[parentTag]?.let {
                        parentTag = it.parentTag
                        removeChildrenRecursively(state, it.tag, dispatcher)
                    }
                }
                state.navigationComponents[action.tag] = ActivityState(tag = action.tag, parentTag = parentTag, requestCode = action.requestCode)
                dispatcher.dispatch(NavigationActions.UpdateTo(state))
                if (action.hideKeyboard) {
                    navigationHandler.hideKeyboard()
                }
                navigationHandler.startActivity(action.tag, action.requestCode)
            }
            is NavigationActions.OpenFragment -> {
                val doomedSiblings = when (action.replaceType) {
                    NavigationActions.FragmentReplaceType.NONE -> {
                        null
                    }
                    NavigationActions.FragmentReplaceType.REPLACE_ALL_VISIBLE_IN_CONTAINER -> {
                        getVisibleChildrenWithContainer(state, action.parentTag, action.containerId)
                    }
                    NavigationActions.FragmentReplaceType.REPLACE_ALL_VISIBLE -> {
                        getVisibleChildren(state, action.parentTag)
                    }
                    NavigationActions.FragmentReplaceType.REPLACE_ALL -> {
                        getChildren(state, action.parentTag)
                    }
                }
                doomedSiblings?.forEach {
                    if (it.key != action.tag && it.value is FragmentState) {
                        removeChildrenRecursively(state, it.key, dispatcher)
                    }
                }
                val sunsetSiblings = when (action.hideType) {
                    NavigationActions.FragmentHideType.HIDE_NONE -> {
                        null
                    }
                    NavigationActions.FragmentHideType.HIDE_ALL_IN_CONTAINER -> {
                        getVisibleChildrenWithContainer(state, action.parentTag, action.containerId)
                    }
                    NavigationActions.FragmentHideType.HIDE_ALL -> {
                        getVisibleChildren(state, action.parentTag)
                    }
                }
                sunsetSiblings?.forEach {
                    if (it.key != action.tag && it.value is FragmentState) {
                        it.value.parentTag?.let { parentTag ->
                            navigationHandler.hideFragment(parentTag, it.value.tag)
                        }
                    }
                }
                state.navigationComponents[action.tag] = FragmentState(tag = action.tag, containerId = action.containerId, parentTag = action.parentTag)
                dispatcher.dispatch(NavigationActions.UpdateTo(state))
                if (action.hideKeyboard) {
                    navigationHandler.hideKeyboard()
                }
                navigationHandler.openFragment(action.parentTag, action.containerId, action.tag)
            }
        }
    }

    // Private Api

    private fun getChildren(state: NavigationState, parentTag: NavigationTag?): Map<NavigationTag, NavigationComponent>? {
        if (parentTag == null) {
            return null
        }
        return state.navigationComponents.filter { it.value.parentTag.toString() == parentTag.toString() }
    }

    private fun getVisibleChildren(state: NavigationState, parentTag: NavigationTag?): Map<NavigationTag, NavigationComponent>? =
        getChildren(state, parentTag)?.filter { it.value.isVisible }

    private fun getVisibleChildrenWithContainer(state: NavigationState, parentTag: NavigationTag?, containerId: NavigationContainerId): Map<NavigationTag, NavigationComponent>? =
        getVisibleChildren(state, parentTag)?.filter { (it.value as? FragmentState)?.containerId.toString() == containerId.toString() }

    private fun getChildrenTagsRecursively(state: NavigationState, parentTag: NavigationTag): List<NavigationTag> {
        val tagList = mutableListOf(parentTag)
        var iterator = 0
        while (iterator < tagList.size) {
            getChildren(state, tagList[iterator])?.forEach {
                tagList.add(it.key)
            }
            iterator += 1
        }
        return tagList.reversed()
    }

    private fun removeChildrenRecursively(state: NavigationState, parentTag: NavigationTag, dispatcher: Dispatcher) {
        getChildrenTagsRecursively(state, parentTag).forEach {
            dispatcher.dispatch(NavigationActions.RemovingNavigationComponent(it))
            val navigationComponent = state.navigationComponents[it]
            if (navigationComponent != null && navigationComponent is FragmentState) {
                navigationHandler.removeFragment(navigationComponent.parentTag, navigationComponent.tag)
            }
            state.navigationComponents.remove(it)
        }
    }

    interface SubStateSupplier<in S, T> {
        /**
         * Specify the sub-state you want to acquire.
         *
         * @param state The state
         * @return <T> The sub-state
         */
        fun getSubState(state: S): T
    }

    interface NavigationHandler {
        fun startActivity(tag: NavigationTag, requestCode: Int?)
        fun openFragment(parentTag: NavigationTag, containerId: NavigationContainerId, tag: NavigationTag)
        fun showDialogFragment(parentTag: NavigationTag, targetTag: NavigationTag, requestCode: Int, tag: NavigationTag)
        fun hideFragment(parentTag: NavigationTag, tag: NavigationTag)
        fun callOnBackPressed(parentTag: NavigationTag, tag: NavigationTag)
        fun removeFragment(parentTag: NavigationTag, tag: NavigationTag)
        fun finishActivity()
        fun hideKeyboard()
    }
}
