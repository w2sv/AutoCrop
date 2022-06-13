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
import com.autocrop.global.BooleanPreferences
import com.autocrop.global.preferencesInstances
import com.autocrop.uicontroller.activity.ApplicationActivity
import com.autocrop.utils.BlankFun
import com.autocrop.utils.numericallyInflected
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

    override fun viewModelFactory(): ViewModelProvider.Factory =
        MainActivityViewModelFactory(
            imageFileIOSynopsis = getIntentExtra<ByteArray>(IntentExtraIdentifier.EXAMINATION_ACTIVITY_RESULTS)?.let {
                ImageFileIOSynopsis(it)
            }
        )

    override fun onSavedInstanceStateNull() {
        if (!BooleanPreferences.welcomeMessageShown){
            onButtonsHalfFadedIn{
                snacky(
                    "Good to have you on board! \uD83D\uDD25 Now select some screenshots and save your first AutoCrops! \uD83D\uDE80",
                    duration = resources.getInteger(R.integer.duration_snackbar_extra_long)
                )
                    .buildAndShow()
            }
            BooleanPreferences.welcomeMessageShown = true
        }
        else{
            sharedViewModel.imageFileIOSynopsis?.run {
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

    private fun ImageFileIOSynopsis.showAsSnackbar(){
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