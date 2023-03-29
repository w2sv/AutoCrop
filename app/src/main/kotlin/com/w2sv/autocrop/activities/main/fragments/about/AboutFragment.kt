package com.w2sv.autocrop.activities.main.fragments.about

import android.os.Bundle
import android.view.View
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.autocrop.BuildConfig
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.databinding.AboutBinding
import com.w2sv.autocrop.ui.views.animate
import java.util.Calendar

class AboutFragment :
    AppFragment<AboutBinding>(AboutBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            copyrightTv.text = resources.getString(R.string.copyright, Calendar.getInstance().get(Calendar.YEAR))
            versionTv.text = resources.getString(R.string.version, BuildConfig.VERSION_NAME)

            appTitleTextView.setOnClickListener { it.animate(Techniques.Wobble) }
            logoIv.setOnClickListener { it.animate(Techniques.Tada) }
            versionTv.setOnClickListener { it.animate(Techniques.RubberBand) }
            copyrightTv.setOnClickListener { it.animate(Techniques.Wave) }
        }
    }
}