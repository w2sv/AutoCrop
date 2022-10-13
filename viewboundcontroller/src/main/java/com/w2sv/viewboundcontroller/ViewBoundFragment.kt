package com.w2sv.viewboundcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class ViewBoundFragment<VB: ViewBinding>(override val bindingClass: Class<VB>) :
    Fragment(),
    ViewBindingInflator<VB> {

    override val binding: VB
        get() = _binding!!
    private var _binding: VB? = null

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