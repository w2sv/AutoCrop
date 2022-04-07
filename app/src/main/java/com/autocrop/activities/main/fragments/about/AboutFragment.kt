package com.autocrop.activities.main.fragments.about

import android.os.Bundle
import android.view.View
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.databinding.ActivityMainFragmentAboutBinding

class AboutFragment: MainActivityFragment<ActivityMainFragmentAboutBinding>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.versionTextView.text = BuildConfig.VERSION_NAME
    }
}