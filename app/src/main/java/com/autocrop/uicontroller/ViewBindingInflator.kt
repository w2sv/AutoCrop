package com.autocrop.uicontroller

import androidx.viewbinding.ViewBinding
import com.autocrop.utils.typeArgument
import java.lang.reflect.Method

interface ViewBindingInflator<VB: ViewBinding> {
    val binding: VB

    @Suppress("UNCHECKED_CAST")
    fun inflateViewBinding(vararg paramWithType: Pair<Any?, Class<*>>): VB{
        val (params, types) = paramWithType.unzip()

        return inflateViewBindingMethod(*types.toTypedArray())
            .invoke(null, *params.toTypedArray()) as VB
    }

    @Suppress("UNCHECKED_CAST")
    private fun inflateViewBindingMethod(vararg parameterTypes: Class<*>): Method =
        (typeArgument() as Class<VB>)
            .getMethod("inflate", *parameterTypes)
}