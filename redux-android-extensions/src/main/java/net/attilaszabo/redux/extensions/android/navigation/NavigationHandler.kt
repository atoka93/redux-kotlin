package net.attilaszabo.redux.extensions.android.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import net.attilaszabo.redux.extensions.android.base.BaseFragment
import net.attilaszabo.redux.extensions.android.domain.navigation.NavigationSideEffect
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationContainerId
import net.attilaszabo.redux.extensions.android.state.navigation.NavigationTag
import kotlin.reflect.KClass

open class NavigationHandler(private val navigationDataSupplier: NavigationDataSupplier) : NavigationSideEffect.NavigationHandler {

    // Members

    private var activity: AppCompatActivity? = null
    private var fragmentManagers: MutableMap<String, FragmentManager?> = mutableMapOf()
    private var bundles: MutableMap<String, Bundle?> = mutableMapOf()

    // NavigationHandler

    override fun startActivity(tag: NavigationTag, requestCode: Int?) {
        activity?.let {
            val intent = Intent(it, navigationDataSupplier.getKotlinClass(tag).java)
            bundles[tag.toString()]?.apply {
                intent.putExtras(this)
                remove(tag.toString())
            }
            if (requestCode == null) {
                it.startActivity(intent)
            } else {
                it.startActivityForResult(intent, requestCode)
            }
            it.startActivity(Intent(it, navigationDataSupplier.getKotlinClass(tag).java))
            it.finish()
        }
    }

    override fun openFragment(parentTag: NavigationTag, containerId: NavigationContainerId, tag: NavigationTag) {
        val supportFragmentManager = findSupportFragmentManager(parentTag)
        var existingFragment: Fragment? = supportFragmentManager.findFragmentByTag(tag.toString())
        if (existingFragment?.isVisible == true) {
            return
        }
        val transaction = supportFragmentManager.beginTransaction()
        if (existingFragment == null) {
            existingFragment = navigationDataSupplier.getFragment(tag)
            bundles[tag.toString()]?.apply {
                existingFragment.arguments = this
                remove(tag.toString())
            }
            transaction.add(navigationDataSupplier.getContainerId(containerId), existingFragment, tag.toString())
        } else {
            transaction.show(existingFragment)
        }
        transaction.commit()
    }

    override fun showDialogFragment(parentTag: NavigationTag, targetTag: NavigationTag, requestCode: Int, tag: NavigationTag) {
        val supportFragmentManager = findSupportFragmentManager(parentTag)
        var existingFragment: Fragment? = supportFragmentManager.findFragmentByTag(tag.toString())
        when {
            existingFragment?.isVisible == true -> return
            existingFragment == null -> existingFragment = navigationDataSupplier.getFragment(tag)
        }
        (existingFragment as? DialogFragment)?.apply {
            setTargetFragment(supportFragmentManager.findFragmentByTag(targetTag.toString()), requestCode)
            bundles[tag.toString()]?.apply {
                arguments = this
                remove(tag.toString())
            }
            show(supportFragmentManager.beginTransaction(), tag.toString())
        }
    }

    override fun hideFragment(parentTag: NavigationTag, tag: NavigationTag) {
        val supportFragmentManager = findSupportFragmentManager(parentTag)
        supportFragmentManager
            .fragments
            .find { it != null && it.view != null && it.tag == tag.toString() }?.let {
                supportFragmentManager.beginTransaction().hide(it).commit()
            }
    }

    override fun callOnBackPressed(parentTag: NavigationTag, tag: NavigationTag) {
        val fragment = findSupportFragmentManager(parentTag)
            .fragments
            .find { it != null && it.isVisible && it.view != null && it.tag == tag.toString() }
        (fragment as? BaseFragment<*, *>)?.onBackPressed()
    }

    override fun removeFragment(parentTag: NavigationTag, tag: NavigationTag) {
        val supportFragmentManager = findSupportFragmentManager(parentTag)
        supportFragmentManager
            .fragments
            .find { it != null && it.view != null && it.tag == tag.toString() }?.let {
                supportFragmentManager.beginTransaction().remove(it).commit()
            }
    }

    override fun finishActivity() {
        activity?.finish()
    }

    override fun hideKeyboard() {
        try {
            val inputMethodManager = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        } catch (_: Exception) {
        }
    }

    // Public Api

    fun setActivity(activity: AppCompatActivity?) {
        this.activity = activity
    }

    fun addFragmentManager(identifier: NavigationTag, fragmentManager: FragmentManager?) {
        fragmentManagers[identifier.toString()] = fragmentManager
    }

    fun addBundle(identifier: NavigationTag, bundle: Bundle) {
        bundles[identifier.toString()] = bundle
    }

    // Private Api

    private fun findSupportFragmentManager(tag: NavigationTag) = fragmentManagers[tag.toString()]
        ?: throw Exception("No match for the fragment manager.")

    interface NavigationDataSupplier {
        fun getKotlinClass(tag: NavigationTag): KClass<*>
        fun getFragment(tag: NavigationTag): Fragment
        fun getContainerId(containerTag: NavigationContainerId): Int
    }
}
