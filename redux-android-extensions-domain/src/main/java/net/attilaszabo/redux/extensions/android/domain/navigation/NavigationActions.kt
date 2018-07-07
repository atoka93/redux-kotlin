package net.attilaszabo.redux.extensions.android.domain.navigation

import net.attilaszabo.redux.Action
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationContainerId
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationState
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationTag

open class NavigationActions : Action {
    data class StartActivity(
        val tag: NavigationTag,
        val replaceCurrentActivity: Boolean = false,
        val requestCode: Int,
        val hideKeyboard: Boolean = true
    ) : NavigationActions()

    data class OpenFragment(
        val tag: NavigationTag,
        val parentTag: NavigationTag,
        val containerId: NavigationContainerId,
        val hideType: FragmentHideType = FragmentHideType.HIDE_ALL_IN_CONTAINER,
        val replaceType: FragmentReplaceType = FragmentReplaceType.NONE,
        val hideKeyboard: Boolean = true
    ) : NavigationActions()

    data class ShowDialogFragment(
        val tag: NavigationTag,
        val parentTag: NavigationTag,
        val targetTag: NavigationTag,
        val requestCode: Int,
        val hideKeyboard: Boolean = true
    ) : NavigationActions()

    enum class FragmentHideType {
        HIDE_NONE, HIDE_ALL_IN_CONTAINER, HIDE_ALL
    }

    enum class FragmentReplaceType {
        NONE, REPLACE_ALL_VISIBLE_IN_CONTAINER, REPLACE_ALL_VISIBLE, REPLACE_ALL
    }

    data class NavigationComponentStarted(
        val tag: NavigationTag
    ) : NavigationActions()

    data class FragmentVisibilityChanged(
        val tag: NavigationTag,
        val isVisible: Boolean
    ) : NavigationActions()

    data class DispatchOnBackPressedToVisibleFragments(
        val parentTag: NavigationTag
    ) : NavigationActions()

    data class CloseFragment(
        val tag: NavigationTag
    ) : NavigationActions()

    object FinishActivity : NavigationActions()

    data class RemovingNavigationComponent(
        val tag: NavigationTag
    ) : NavigationActions()

    internal data class UpdateTo(
        val state: NavigationState
    ) : NavigationActions()

    object HideKeyboard : NavigationActions()
}
