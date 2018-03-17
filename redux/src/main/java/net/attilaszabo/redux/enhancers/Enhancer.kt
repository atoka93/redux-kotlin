package net.attilaszabo.redux.enhancers

import net.attilaszabo.redux.Store.Creator

/**
 * An interface that composes a store creator to return a new, enhanced store creator.
 *
 * @param <S> The state type
 */
interface Enhancer<S> {

    /**
     * @param next The next store creator to compose
     * @return The composed store creator
     */
    fun enhance(next: Creator<S>): Creator<S>
}
