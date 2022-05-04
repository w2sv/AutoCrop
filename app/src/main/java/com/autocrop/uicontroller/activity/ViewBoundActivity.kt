package com.autocrop.uicontroller.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.autocrop.uicontroller.ViewBindingInflator

abstract class ViewBoundActivity<VB: ViewBinding> :
    FragmentActivity(),
    ViewBindingInflator<VB>{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }

    @Suppress("UNCHECKED_CAST")
    override val binding: VB by lazy {
        super.inflateViewBinding(layoutInflater to LayoutInflater::class.java)
    }
}