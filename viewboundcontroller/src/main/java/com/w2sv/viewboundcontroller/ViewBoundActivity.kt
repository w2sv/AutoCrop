package com.w2sv.viewboundcontroller

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.w2sv.viewboundcontroller.databinding.ActivityBinding

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