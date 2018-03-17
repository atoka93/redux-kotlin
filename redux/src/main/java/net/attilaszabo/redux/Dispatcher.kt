package net.attilaszabo.redux

/**
 * A dispatcher accepts an action or an async action; it then may or may not dispatch one or more actions to the store.
 */
interface Dispatcher {

    /**
     * Dispatches an action. This is the only way to trigger a state change.
     *
     * @param action A plain object, an [Action]
     */
    fun dispatch(action: Action)
}
