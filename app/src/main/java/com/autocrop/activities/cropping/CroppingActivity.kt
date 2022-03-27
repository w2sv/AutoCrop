package com.autocrop.activities.cropping

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.cropping.fragments.cropping.CroppingFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.utils.android.returnTransitionAnimation
import com.w2sv.autocrop.databinding.ActivityCroppingBinding

class CroppingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCroppingBinding
    private lateinit var viewModel: CroppingActivityViewModel

    private val croppingFragment: CroppingFragment by lazy{ CroppingFragment() }

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

        // ----------launch cropping fragment
        if (savedInstanceState == null)
            supportFragmentManager
                .beginTransaction()
                .add(binding.activityCroppingLayout.id, croppingFragment)
                .commit()
    }

    fun replaceCurrentFragmentWith(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(binding.activityCroppingLayout.id, fragment)
            .setReorderingAllowed(true)
            .commit()
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
    private val backPressHandler = BackPressHandler(this, "Tap again to cancel") {
        croppingFragment.cropper.cancel(false)
        returnToMainActivity()
    }

    override fun onBackPressed() {
        if (croppingFragment.cropper.status != AsyncTask.Status.FINISHED)
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