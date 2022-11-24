package com.w2sv.autocrop.controller.activity

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.blogspot.atifsoftwares.animatoolib.R
import com.w2sv.androidutils.ActivityRetriever

abstract class FragmentHostingActivity(private val rootFragmentClass: Class<out Fragment>) : BlankViewBoundActivity() {

    companion object {
        private const val ROOT_FRAGMENT_TAG = "ROOT_FRAGMENT"
    }

    interface Retriever : ActivityRetriever {

        val fragmentHostingActivity: FragmentHostingActivity

        class Implementation(context: Context) : Retriever,
                                                 ActivityRetriever.Implementation(context) {
            override val fragmentHostingActivity: FragmentHostingActivity = activity as FragmentHostingActivity
        }
    }

    private val layoutId: Int get() = binding.root.id

    @SuppressLint("CommitTransaction")
    protected fun launchRootFragment() {
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .add(
                layoutId,
                rootFragmentClass.newInstance(),
                ROOT_FRAGMENT_TAG
            )
            .commit()
    }

    //    @Suppress("UNCHECKED_CAST")
    //    fun <RF : Fragment> getRootFragment(): RF? =
    //        supportFragmentManager.findFragmentByTag(ROOT_FRAGMENT_TAG) as RF?

    fun getCurrentFragment(): Fragment? =
        supportFragmentManager.findFragmentById(layoutId)

    @Suppress("UNCHECKED_CAST")
    fun <F : Fragment> getCastCurrentFragment(): F? =
        getCurrentFragment() as F?

    @SuppressLint("CommitTransaction")
    fun fragmentReplacementTransaction(fragment: Fragment, animated: Boolean = false): FragmentTransaction =
        supportFragmentManager
            .beginTransaction()
            .apply {
                if (animated)
                    setCustomAnimations(
                        R.anim.animate_in_out_enter,
                        R.anim.animate_in_out_exit,
                        R.anim.animate_in_out_enter,
                        R.anim.animate_in_out_exit
                    )
            }
            .setReorderingAllowed(true)
            .replace(layoutId, fragment)
}