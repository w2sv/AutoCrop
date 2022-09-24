package com.autocrop.activities.main

import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import androidx.core.text.color
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.dataclasses.IOSynopsis
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.preferences.preferencesInstances
import com.autocrop.ui.controller.activity.ApplicationActivity
import com.autocrop.utils.kotlin.BlankFun
import com.autocrop.utils.kotlin.extensions.numericallyInflected
import com.autocrop.utils.android.buildAndShow
import com.autocrop.utils.android.getThemedColor
import com.autocrop.utils.android.snacky
import com.google.android.play.core.review.ReviewManagerFactory
import com.w2sv.autocrop.R
import timber.log.Timber

class MainActivity :
    ApplicationActivity<FlowFieldFragment, MainActivityViewModel>(
        FlowFieldFragment::class.java,
        MainActivityViewModel::class,
        accessedPreferenceInstances = preferencesInstances) {

    override fun viewModelFactory(): ViewModelProvider.Factory =
        MainActivityViewModelFactory(
            IOSynopsis = getIntentExtra<ByteArray>(IntentExtraIdentifier.EXAMINATION_ACTIVITY_RESULTS)?.let {
                IOSynopsis.fromByteArray(it)
            }
        )

    override fun onSavedInstanceStateNull() {
        super.onSavedInstanceStateNull()

        if (!BooleanPreferences.welcomeMessageShown)
            onButtonsHalfFadedIn{
                snacky(
                    "Good to have you on board! \uD83D\uDD25 Now select some screenshots and save your first AutoCrops! \uD83D\uDE80",
                    duration = resources.getInteger(R.integer.duration_snackbar_extra_long)
                )
                    .buildAndShow()
            }
        else{
            sharedViewModel.IOSynopsis?.run {
                val showAsSnackbarOnButtonsHalfFadedIn: BlankFun = { onButtonsHalfFadedIn { showAsSnackbar() } }

                if (nSavedCrops != 0)
                    launchReviewFlow(showAsSnackbarOnButtonsHalfFadedIn)
                else
                    showAsSnackbarOnButtonsHalfFadedIn()
            }
        }
    }

    private fun launchReviewFlow(onFinishedListener: BlankFun){
        with(ReviewManagerFactory.create(this)){
            requestReviewFlow()
                .addOnCompleteListener { task ->
                    task.result?.let {
                        launchReviewFlow(this@MainActivity, it)
                            .addOnCompleteListener{ onFinishedListener() }
                    } ?: run {
                        Timber.i(task.exception)
                        onFinishedListener()
                    }
            }
        }
    }

    private fun IOSynopsis.showAsSnackbar(){
        val textAndIcon = if (nSavedCrops == 0)
            "Discarded all crops" to R.drawable.ic_outline_sentiment_dissatisfied_24
        else
            SpannableStringBuilder().apply {
                append("Saved $nSavedCrops ${"crop".numericallyInflected(nSavedCrops)} to ")
                color(getThemedColor(R.color.notification_success)) {append(cropWriteDirIdentifier)}
                if (nDeletedScreenshots != 0)
                    append(
                        " and deleted ${
                            if (nDeletedScreenshots == nSavedCrops)
                                "corresponding"
                            else
                                nDeletedScreenshots
                        } ${"screenshot".numericallyInflected(nDeletedScreenshots)}"
                    )
            } to R.drawable.ic_baseline_done_24

        with(textAndIcon){
            snacky(first)
                .setIcon(second)
                .buildAndShow()
        }
    }

    private fun onButtonsHalfFadedIn(runnable: Runnable){
        Handler(Looper.getMainLooper()).postDelayed(
            runnable,
            resources.getInteger(R.integer.duration_fade_in_flowfield_fragment_buttons).toLong() / 2
        )
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