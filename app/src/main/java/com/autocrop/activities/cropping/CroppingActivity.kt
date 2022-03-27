package com.autocrop.activities.cropping

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.cropping.fragments.cropping.CroppingRootFragment
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.clearAndLog
import com.autocrop.utils.android.returnTransitionAnimation
import com.w2sv.autocrop.databinding.ActivityCroppingBinding

class CroppingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCroppingBinding

    private lateinit var viewModel: CroppingActivityViewModel

    private val rootFragment: CroppingRootFragment by lazy{ CroppingRootFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -----------retrieve ViewBinding, setContentView
        binding = ActivityCroppingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // -----------retrieve screenshot uris from intent and retrieve viewModel
        viewModel = ViewModelProvider(
            this,
            CroppingActivityViewModelFactory(
                uris = intent.getParcelableArrayListExtra(IntentIdentifiers.SELECTED_IMAGE_URI_STRINGS)!!
            )
        )[CroppingActivityViewModel::class.java]

        // ----------launch root fragment
        supportFragmentManager
            .beginTransaction()
            .add(binding.layout.id, rootFragment)
            .commit()
    }

    fun replaceCurrentFragmentWith(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(binding.layout.id, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    fun startMainActivity() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        returnTransitionAnimation()
    }

    /**
     * Return to MainActivity on confirmed back press
     */
    private val backPressHandler = BackPressHandler(this, "Tap again to cancel") {
        rootFragment.cropper.cancel(false)
        ExaminationActivity.cropBundles.clearAndLog()

        startMainActivity()
    }

    override fun onBackPressed() {
        if (rootFragment.cropper.status != AsyncTask.Status.FINISHED)
            return backPressHandler()
    }

    /**
     * Reset progress bar progress on stop
     */
    override fun onStop() {
        super.onStop()

        finishAndRemoveTask()
    }
}