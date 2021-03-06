package net.attilaszabo.redux.implementation.java

import net.attilaszabo.redux.Action
import net.attilaszabo.redux.Reducer
import net.attilaszabo.redux.Store
import net.attilaszabo.redux.Subscriber
import net.attilaszabo.redux.enhancers.Enhancer
import java.util.concurrent.Executors

/**
 * Asynchronous extension of the BaseStore.
 *
 * @param <S> The state type
 * @param initialState The initial state
 * @param reducer The [Reducer]
 */
class AsyncStore<S>
private constructor(initialState: S, reducer: Reducer<S>, private val mainThreadExecutor: (Runnable) -> Unit) : BaseStore<S>(initialState, reducer) {

    // Store

    override fun dispatch(action: Action) {
        if (isReducing.compareAndSet(false, true)) {
            try {
                val previousState = storedState
                storedState = reducer.reduce(storedState, action)

                if (previousState != storedState) {
                    subscribers.forEach {
                        mainThreadExecutor(Runnable { it.onStateChanged(storedState) })
                    }
                }
            } catch (e: Exception) {
                mainThreadExecutor(Runnable { throw IllegalStateException("Dispatch exception: ${e.message}") })
            } finally {
                isReducing.set(false)
            }
        }
    }

    // Creator

    class Creator<S>(private val mainThreadExecutor: (Runnable) -> Unit = { it.run() }) : Store.Creator<S> {

        override fun create(initialState: S, reducer: Reducer<S>) = AsyncStore(initialState, reducer, mainThreadExecutor)
    }

    companion object {

        /**
         * Create an [AsyncStore], a store where the dispatch functions are executed on a different
         * thread, but the new state distribution and errors are delivered on the main thread.
         *
         * @param <S> The state type
         * @param mainThreadExecutor A function that executes a runnable
         * @param initialState The initial state
         * @param reducer The [Reducer]
         * @param [enhancers] A list of enhancers
         * @return The store
         */
        fun <S> create(
            mainThreadExecutor: (Runnable) -> Unit, initialState: S,
            reducer: Reducer<S>, vararg enhancers: Enhancer<S>
        ): Store<S> =
            createStore(Creator(mainThreadExecutor), initialState, reducer, *enhancers, applyAsyncDispatch())

        /**
         * An [Enhancer] to submit dispatch commands to an executor to execute on a separate thread.
         *
         * @return An [Enhancer]
         */
        private fun <S> applyAsyncDispatch(): Enhancer<S> = object : Enhancer<S> {
            override fun enhance(next: Store.Creator<S>) = object : Store.Creator<S> {
                override fun create(initialState: S, reducer: Reducer<S>): Store<S> = object : Store<S>(initialState, reducer) {

                    // Members

                    private val store = next.create(initialState, reducer)
                    private val executorService = Executors.newSingleThreadExecutor()

                    // Store

                    override fun subscribe(subscriber: Subscriber<S>) = store.subscribe(subscriber)

                    override fun getState(): S = store.getState()

                    override fun replaceReducer(reducer: Reducer<S>) = store.replaceReducer(reducer)

                    override fun dispatch(action: Action) {
                        executorService.submit {
                            store.dispatch(action)
                        }
                    }
                }
            }
        }
    }
}
