package com.autocrop.activities.cropping

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.FragmentHostingActivity
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.cropping.fragments.cropping.CroppingFragment
import com.autocrop.activities.cropping.fragments.croppingunsuccessful.CroppingUnsuccessfulFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.returnTransitionAnimation
import com.w2sv.autocrop.databinding.ActivityCroppingBinding

class CroppingActivity : FragmentHostingActivity<ActivityCroppingBinding>(ActivityCroppingBinding::inflate) {
    private lateinit var viewModel: CroppingActivityViewModel

    override val rootFragment: CroppingFragment by lazy{ CroppingFragment() }
    val croppingUnsuccessfulFragment: CroppingUnsuccessfulFragment by lazy{ CroppingUnsuccessfulFragment() }

    override fun onCreateCore() {
        fragmentContainerViewId = binding.layout.id

        viewModel = ViewModelProvider(
            this,
            CroppingActivityViewModelFactory(
                uris = intent.getParcelableArrayListExtra(IntentIdentifiers.SELECTED_IMAGE_URI_STRINGS)!!
            )
        )[CroppingActivityViewModel::class.java]
    }

    fun returnToMainActivity() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        returnTransitionAnimation()
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
        if (croppingUnsuccessfulFragment.isVisible){
            returnToMainActivity()
        }
        else if (rootFragment.isVisible)
            handleBackPress()
    }

    override fun onStop() {
        super.onStop()

        finishAndRemoveTask()
    }
}