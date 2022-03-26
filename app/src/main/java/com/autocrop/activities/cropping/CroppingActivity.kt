package com.autocrop.activities.cropping

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.clearAndLog
import com.autocrop.utils.android.*
import com.autocrop.utils.logBeforehand
import com.w2sv.autocrop.databinding.ActivityCroppingBinding
import java.lang.ref.WeakReference

class CroppingActivity : AppCompatActivity() {
    private lateinit var cropper: Cropper

    private lateinit var binding: ActivityCroppingBinding
    private val ActivityCroppingBinding.croppingViews: List<View>
        get() = listOf(croppingProgressBar, croppingCurrentImageNumberTextView, croppingTextView)
    private val ActivityCroppingBinding.croppingFailureViews: List<View>
        get() = listOf(croppingFailureTextView, croppingFailureErrorIcon)

    private lateinit var viewModel: CroppingActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -----------retrieve ViewBinding, setContentView
        binding = ActivityCroppingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // convert passed image uri strings back to uris, set nSelectedImages
        intent.getParcelableArrayListExtra<Uri>(IntentIdentifiers.SELECTED_IMAGE_URI_STRINGS)!!.let { uris ->
            viewModel = ViewModelProvider(
                this,
                CroppingActivityViewModelFactory(
                    uris.size,
                    binding.croppingProgressBar.max
                )
            )[CroppingActivityViewModel::class.java]

            // execute async cropping task
            cropper = Cropper(
                viewModel,
                WeakReference(binding.croppingProgressBar),
                WeakReference(binding.croppingCurrentImageNumberTextView),
                contentResolver,
                ::onTaskCompleted
            ).apply {
                execute(*uris.toTypedArray())
            }
        }
    }

    /**
     * Starts either Examination- or MainActivity depending on whether or not any
     * of the selected images has been successfully cropped
     */
    private fun onTaskCompleted() = logBeforehand("Async Cropping task finished") {
        if (ExaminationActivity.cropBundles.isNotEmpty())
            startExaminationActivity(viewModel.nSelectedImages - ExaminationActivity.cropBundles.size)
        else
            {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        hideSystemUI(window)

                        binding.croppingViews.forEach { it.hide() }
                        binding.croppingFailureTextView.updateText(viewModel.nSelectedImages > 1)
                        binding.croppingFailureViews.forEach { it.show() }

                        Handler(Looper.getMainLooper()).postDelayed(
                            { startMainActivity() },
                            3000
                        )
                    },
                    300
                )
            }
    }

    private fun startExaminationActivity(nDismissedCrops: Int) {
        startActivity(
            Intent(this, ExaminationActivity::class.java).putExtra(
                IntentIdentifiers.N_DISMISSED_IMAGES,
                nDismissedCrops
            )
        )
        proceedTransitionAnimation()
    }

    private fun startMainActivity() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        returnTransitionAnimation()
    }

    /**
     * Return to MainActivity on confirmed back press
     */
    private val backPressHandler = BackPressHandler(this, "Tap again to cancel") {
        cropper.cancel(false)
        ExaminationActivity.cropBundles.clearAndLog()

        startMainActivity()
    }

    override fun onBackPressed() {
        if (cropper.status != AsyncTask.Status.FINISHED)
            return backPressHandler()
    }

    /**
     * Reset progress bar progress on stop
     */
    override fun onStop() {
        super.onStop()

        binding.croppingProgressBar.progress = 0
        finishAndRemoveTask()
    }
}