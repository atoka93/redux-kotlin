package net.attilaszabo.redux.extensions.android.base

import android.os.Bundle
import android.support.v4.app.Fragment

abstract class BaseFragment<S, TViewModel : BaseViewModel<S>> : Fragment() {

    // Members

    protected lateinit var viewModel: TViewModel

    // Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!::viewModel.isInitialized) {
            viewModel = onCreateViewModel()
        }
        (activity?.application as BaseApplication<S>).setFragmentManager(viewModel.navigationComponentTag, childFragmentManager)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!::viewModel.isInitialized) {
            viewModel = onCreateViewModel()
        }
        viewModel.onCreate((activity?.application as BaseApplication<S>).getStore())
    }

    override fun onHiddenChanged(isHidden: Boolean) {
        super.onHiddenChanged(isHidden)
        viewModel.fragmentVisibilityChanged(isHidden)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        viewModel.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity?.application as BaseApplication<S>).setFragmentManager(viewModel.navigationComponentTag, null)
    }

    open fun onBackPressed() {
        viewModel.onBackPressed()
    }

    // Public Api

    abstract fun onCreateViewModel(): TViewModel
}
