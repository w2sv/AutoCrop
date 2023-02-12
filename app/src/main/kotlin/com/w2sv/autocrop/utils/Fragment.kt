package com.w2sv.autocrop.utils

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

fun <F : Fragment> getFragment(clazz: Class<F>, vararg bundlePairs: Pair<String, Any?>): F =
    clazz.newInstance()
        .apply {
            arguments = bundleOf(*bundlePairs)
        }