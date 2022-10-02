package com.autocrop.ui.controller.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.autocrop.ui.controller.ViewBindingInflator
import com.w2sv.autocrop.databinding.ActivityBinding

abstract class ViewBoundActivity :
    AppCompatActivity(),
    ViewBindingInflator<ActivityBinding> {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
    }

    override val binding: ActivityBinding by lazy {
        super.inflateViewBinding(layoutInflater to LayoutInflater::class.java)
    }

    override val bindingClass = ActivityBinding::class.java
}