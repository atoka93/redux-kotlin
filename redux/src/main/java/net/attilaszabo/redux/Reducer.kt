package net.attilaszabo.redux

/**
 * A reducer specifies how the application's state changes in response to actions sent to the store.
 *
 * @param <S> The state type
 */
interface Reducer<S> {

    /**
     * A pure function which returns a new state given the previous state and an action.
     *
     * Things you should *never* do inside a reducer:
     *  * Mutate its arguments
     *  * Perform side effects like API calls and routing transitions
     *  * Call non-pure functions, e.g. Date() or Random.nextInt()
     *
     * Given the same arguments, it should always result in the same output.
     *
     * @param state The previous state
     * @param action The dispatched action
     * @return The new state
     */
    fun reduce(state: S, action: Action): S

    companion object {

        /**
         * Combines multiple reducers into a single reducer.
         *
         * @param <S> The state type
         * @param [reducers] A list of reducers
         * @return The combined reducer
         */
        fun <S> chainReducers(vararg reducers: Reducer<S>): Reducer<S> = object : Reducer<S> {
            override fun reduce(state: S, action: Action): S = reducers.fold(state) { reducerState, reducer ->
                reducer.reduce(reducerState, action)
            }
        }
    }
}
