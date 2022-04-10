package com.autocrop.activities.cropping

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.IntentIdentifier
import com.autocrop.activities.cropping.fragments.cropping.CroppingFragment
import com.autocrop.activities.cropping.fragments.croppingunsuccessful.CroppingUnsuccessfulFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.uicontroller.activity.FragmentHostingActivity
import com.autocrop.utils.android.BackPressHandler
import com.w2sv.autocrop.databinding.ActivityCroppingBinding

class CroppingActivity : FragmentHostingActivity<ActivityCroppingBinding>() {
    private lateinit var viewModel: CroppingActivityViewModel

    override val rootFragment: CroppingFragment by lazy{ CroppingFragment() }
    val croppingUnsuccessfulFragment: CroppingUnsuccessfulFragment by lazy{ CroppingUnsuccessfulFragment() }

    override fun onCreateCore() {
        viewModel = ViewModelProvider(
            this,
            CroppingActivityViewModelFactory(
                uris = intent.getParcelableArrayListExtra(IntentIdentifier.SELECTED_IMAGE_URI_STRINGS)!!
            )
        )[CroppingActivityViewModel::class.java]
    }

    fun returnToMainActivity() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        ActivityTransitions.RESTART(this)
    }

    /**
     * Return to MainActivity on confirmed back press
     */
    private val handleBackPress = BackPressHandler(this, "Tap again to cancel") {
        rootFragment.cropper.cancel(false)
        returnToMainActivity()
    }

    /**
     * Return directly to [MainActivity] if [croppingUnsuccessfulFragment] visible,
     * otherwise [returnToMainActivity] upon confirmed back press
     */
    override fun onBackPressed() {
        if (croppingUnsuccessfulFragment.isVisible)
            returnToMainActivity()
        else if (rootFragment.isVisible)
            handleBackPress()
    }

    override fun onStop() {
        super.onStop()

        finishAndRemoveTask()
    }
}