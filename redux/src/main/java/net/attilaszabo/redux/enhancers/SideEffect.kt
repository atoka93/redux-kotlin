package net.attilaszabo.redux.enhancers

import net.attilaszabo.redux.Action
import net.attilaszabo.redux.Dispatcher

/**
 * A [Middleware] that produces additional actions after the action at hand is dispatched.
 *
 * @param <S> The state type
 */
abstract class SideEffect<in S> : Middleware<S> {

    override fun dispatch(state: () -> S, action: Action, next: Dispatcher, dispatcher: Dispatcher) {
        next.dispatch(action)
        this@SideEffect.dispatch(state, action, dispatcher)
    }

    /**
     * A function that can dispatch additional actions based on the received state and action.
     *
     * @param <S> The state type
     * @param state The state
     * @param action An [Action]
     * @param dispatcher The original dispatcher to dispatch additional actions
     */
    abstract fun dispatch(state: () -> S, action: Action, dispatcher: Dispatcher)
}
