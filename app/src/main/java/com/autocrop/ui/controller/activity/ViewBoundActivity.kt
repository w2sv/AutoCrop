package com.autocrop.ui.controller.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import com.autocrop.ui.controller.ViewBindingInflator
import com.w2sv.autocrop.databinding.ActivityBinding

abstract class ViewBoundActivity :
    FragmentActivity(),
    ViewBindingInflator<ActivityBinding> {

    override val bindingClass = ActivityBinding::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }

    @Suppress("UNCHECKED_CAST")
    override val binding: ActivityBinding by lazy {
        super.inflateViewBinding(layoutInflater to LayoutInflater::class.java)
    }
}