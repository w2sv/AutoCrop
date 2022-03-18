package com.autocrop.activities.examination.fragments

import androidx.fragment.app.FragmentManager
import com.autocrop.utils.get
import com.w2sv.autocrop.R

abstract class DownstreamExaminationActivityFragment(layoutId: Int): ExaminationActivityFragment(layoutId) {
    fun invoke(flipRight: Boolean, supportFragmentManager: FragmentManager) {
        val animations = arrayOf(
            arrayOf(
                R.animator.card_flip_left_in,
                R.animator.card_flip_left_out
            ), arrayOf(
                R.animator.card_flip_right_in,
                R.animator.card_flip_right_out
            )
        )[flipRight]

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(animations[0], animations[1])
            .replace(R.id.container, this)
            .addToBackStack(null)
            .commit()
    }
}