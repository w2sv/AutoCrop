package com.autocrop.activities

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding

abstract class ViewBindingHandlingActivity<VB: ViewBinding>(private val inflateViewBinding: (LayoutInflater) -> VB): FragmentActivity(){
    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = inflateViewBinding(layoutInflater)
        setContentView(binding.root)
    }
}