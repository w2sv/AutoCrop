package com.w2sv.viewboundcontroller

import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method

interface ViewBindingInflator<VB: ViewBinding> {
    val binding: VB
    val bindingClass: Class<VB>

    fun inflateViewBinding(vararg paramWithType: Pair<Any?, Class<*>>): VB{
        val (params, types) = paramWithType.unzip()

        @Suppress("UNCHECKED_CAST")
        return getInflateViewBinding(*types.toTypedArray())
            .invoke(null, *params.toTypedArray()) as VB
    }

    private fun getInflateViewBinding(vararg parameterTypes: Class<*>): Method =
        bindingClass
            .getMethod("inflate", *parameterTypes)
}