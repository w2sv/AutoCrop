package com.autocrop.ui.elements.view

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.autocrop.utils.kotlin.BlankFun

fun View.show() { visibility = View.VISIBLE }
fun View.remove() { visibility = View.GONE }

inline fun View.ifNotInEditMode(f: BlankFun){
    if (!isInEditMode)
        f()
}

inline fun <reified VM: ViewModel>View.viewModelLazy(): Lazy<VM> =
    lazy { ViewModelProvider(findViewTreeViewModelStoreOwner()!!)[VM::class.java] }

inline fun <reified VM: ViewModel>View.viewModel(): VM =
    viewModelLazy<VM>().value

inline fun <reified VM: ViewModel>View.activityViewModelLazy(): Lazy<VM> =
    lazy { ViewModelProvider(context as ViewModelStoreOwner)[VM::class.java] }

inline fun <reified VM: ViewModel>View.activityViewModel(): VM =
    activityViewModelLazy<VM>().value