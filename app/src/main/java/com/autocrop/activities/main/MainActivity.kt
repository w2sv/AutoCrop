package com.autocrop.activities.main

import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import androidx.core.text.color
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.collections.ImageFileIOSynopsis
import com.autocrop.global.preferencesInstances
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.utils.numericallyInflected
import com.autocrop.utilsandroid.NotificationColor
import com.autocrop.utilsandroid.buildAndShow
import com.autocrop.utilsandroid.getThemedColor
import com.autocrop.utilsandroid.snacky
import com.google.android.play.core.review.ReviewManagerFactory
import com.w2sv.autocrop.R
import timber.log.Timber

class MainActivity :
    ApplicationActivity<FlowFieldFragment, MainActivityViewModel>(
        FlowFieldFragment::class.java,
        MainActivityViewModel::class.java,
        accessedPreferenceInstances = preferencesInstances) {

    override fun onSavedInstanceStateNull() {
        super.onSavedInstanceStateNull()

        if (sharedViewModel.imageFileIOSynopsis?.nSavedCrops != 0)
            launchReviewFlow()
    }

    override fun viewModelFactory(): ViewModelProvider.Factory =
        MainActivityViewModelFactory(
            imageFileIOSynopsis = getIntentExtra<ByteArray>(IntentExtraIdentifier.EXAMINATION_ACTIVITY_RESULTS)?.let {
                ImageFileIOSynopsis(it)
            }
        )

    private fun launchReviewFlow(){
        val manager = ReviewManagerFactory.create(this)

        manager
            .requestReviewFlow()
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    task.result?.let {
                        manager.launchReviewFlow(this, it)
                    }
                else
                    Timber.i(task.exception)
            }
    }

    /**
     * Notifies as to IO results from previous ExaminationActivity cycle
     */
    override fun showEntrySnackbar(){
        sharedViewModel.imageFileIOSynopsis?.run {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    when (nSavedCrops) {
                        0 -> snacky("Discarded all crops")
                            .setIcon(R.drawable.ic_outline_sentiment_dissatisfied_24)
                        else -> snacky(
                            SpannableStringBuilder().apply {
                                append("Saved $nSavedCrops ${"crop".numericallyInflected(nSavedCrops)} to ")
                                color(this@MainActivity.getThemedColor(NotificationColor.SUCCESS)){
                                    append(cropWriteDirIdentifier)
                                }

                                if (nDeletedScreenshots != 0)
                                    append(" and deleted ${if (nDeletedScreenshots == nSavedCrops) "corresponding" else nDeletedScreenshots} ${"screenshot".numericallyInflected(nDeletedScreenshots)}")
                            }
                        )
                            .setIcon(R.drawable.ic_baseline_done_24)
                    }
                        .buildAndShow()
                },
                resources.getInteger(R.integer.duration_fade_in_flowfield_fragment_buttons).toLong() / 2
            )
        }
    }

    /**
     * invoke [FlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override fun onBackPressed(){
        when (currentFragment()){
            is AboutFragment -> supportFragmentManager.popBackStack()
            else -> finishAffinity()
        }
    }
}