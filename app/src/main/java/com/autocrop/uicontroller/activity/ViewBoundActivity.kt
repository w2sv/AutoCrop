package com.autocrop.uicontroller.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.autocrop.uicontroller.ViewBindingInflator
import com.w2sv.autocrop.databinding.ActivityBinding

abstract class ViewBoundActivity :
    FragmentActivity(),
    ViewBindingInflator<ActivityBinding>{

    override val viewBindingClass = ActivityBinding::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }

    @Suppress("UNCHECKED_CAST")
    override val binding: ActivityBinding by lazy {
        super.inflateViewBinding(layoutInflater to LayoutInflater::class.java)
    }
}