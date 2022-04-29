package com.autocrop.uicontroller.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class ViewBindingHandlingActivity<VB: ViewBinding>
    : FragmentActivity(){

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB by lazy {
        ((javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VB>)
            .getMethod("inflate", LayoutInflater::class.java)
            .invoke(null, layoutInflater) as VB
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }
}