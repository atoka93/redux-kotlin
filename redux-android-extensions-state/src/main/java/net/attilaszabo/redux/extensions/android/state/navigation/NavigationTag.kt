package net.attilaszabo.redux.extensions.android.state.navigation

abstract class NavigationTag {
    override fun toString(): String = this::class.java.name
}
