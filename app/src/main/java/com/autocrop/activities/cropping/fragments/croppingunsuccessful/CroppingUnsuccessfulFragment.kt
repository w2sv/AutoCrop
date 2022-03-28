package com.autocrop.activities.cropping.fragments.croppingunsuccessful

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.autocrop.activities.cropping.fragments.CroppingActivityFragment
import com.autocrop.utils.android.hideSystemUI
import com.w2sv.autocrop.databinding.ActivityCroppingFragmentCroppingUnsuccessfulBinding

class CroppingUnsuccessfulFragment
    : CroppingActivityFragment<ActivityCroppingFragmentCroppingUnsuccessfulBinding>(ActivityCroppingFragmentCroppingUnsuccessfulBinding::inflate) {

    private val mainActivityReturnHandler = Handler(Looper.getMainLooper())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideSystemUI(requireActivity().window)

        mainActivityReturnHandler.postDelayed(
            { activity.returnToMainActivity() },
            3000
        )
    }

    /**
     * Cancel [mainActivityReturnHandler]
     */
    override fun onStop() {
        super.onStop()

        mainActivityReturnHandler.removeCallbacksAndMessages(null)
    }
}