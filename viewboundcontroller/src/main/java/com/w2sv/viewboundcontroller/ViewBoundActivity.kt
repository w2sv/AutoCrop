package com.w2sv.viewboundcontroller

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class ViewBoundActivity<VB: ViewBinding>(override val bindingClass: Class<VB>) :
    AppCompatActivity(),
    ViewBindingInflator<VB> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }

    override val binding: VB by lazy {
        super.inflateViewBinding(layoutInflater to LayoutInflater::class.java)
    }
}