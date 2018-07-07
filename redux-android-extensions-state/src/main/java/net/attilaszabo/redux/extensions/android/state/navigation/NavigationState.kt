package net.attilaszabo.redux.extensions.android.state.navigation

import java.io.Serializable

data class NavigationState(
    val activeActivityTag: NavigationTag?,
    val navigationComponents: MutableMap<NavigationTag, NavigationComponent> = LinkedHashMap()
) : Serializable

data class ActivityState(
    override val tag: NavigationTag,
    val requestCode: Int,
    override val isVisible: Boolean = true,
    override val parentTag: NavigationTag? = null
) : NavigationComponent(tag, isVisible, parentTag), Serializable

data class FragmentState(
    override val tag: NavigationTag,
    val containerId: NavigationContainerId,
    override val isVisible: Boolean = true,
    override val parentTag: NavigationTag
) : NavigationComponent(tag, isVisible, parentTag), Serializable

open class NavigationComponent(
    open val tag: NavigationTag,
    open val isVisible: Boolean = true,
    open val parentTag: NavigationTag? = null
)
