package com.autocrop.uicontroller.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.autocrop.uicontroller.ViewBindingInflator
import java.lang.reflect.Method

abstract class ViewBindingHoldingFragment<A: Activity, VB: ViewBinding> :
    ExtendedFragment<A>(),
    ViewBindingInflator<VB>{

    private var _binding: VB? = null
    override val binding: VB
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        _binding = super.inflateViewBinding(
            layoutInflater to LayoutInflater::class.java,
            container to ViewGroup::class.java,
            false to Boolean::class.java
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}