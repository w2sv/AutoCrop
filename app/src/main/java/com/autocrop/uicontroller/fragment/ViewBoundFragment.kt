package com.autocrop.uicontroller.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.autocrop.uicontroller.ViewBindingInflator
import com.autocrop.utils.typeArgument

abstract class ViewBoundFragment<VB: ViewBinding> :
    Fragment(),
    ViewBindingInflator<VB>{

    @Suppress("UNCHECKED_CAST")
    override val bindingClass = typeArgument() as Class<VB>

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