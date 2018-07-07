package net.attilaszabo.redux.extensions.android.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class BaseActivity<S, TViewModel : BaseViewModel<S>> : AppCompatActivity() {

    // Members

    protected lateinit var viewModel: TViewModel

    // AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = onCreateViewModel().apply {
            onCreate((application as BaseApplication<S>).getStore())
        }
        (application as BaseApplication<S>).setActiveActivity(viewModel.navigationComponentTag, this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        (application as BaseApplication<S>).setActiveActivity(viewModel.navigationComponentTag, this)
    }

    override fun onPause() {
        viewModel.onPause()
        (application as BaseApplication<S>).setActiveActivity(viewModel.navigationComponentTag, null)
        super.onPause()
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    // Public Api

    abstract fun onCreateViewModel(): TViewModel
}
