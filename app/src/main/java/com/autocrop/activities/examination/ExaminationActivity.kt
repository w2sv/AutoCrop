/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.autocrop.CropBundle
import com.autocrop.UserPreferences
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.SystemUiHidingFragmentActivity
import com.autocrop.activities.examination.fragments.singleaction.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.singleaction.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.get
import com.autocrop.utils.logAfterwards
import com.autocrop.utils.notNull
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationBinding

class ExaminationActivity : SystemUiHidingFragmentActivity() {

    companion object{
        lateinit var cropBundles: MutableList<CropBundle>
    }

    private lateinit var viewModel: ExaminationViewModel

    private val nDismissedImagesRetriever = IntentExtraRetriever<Int>()

    private lateinit var binding: ActivityExaminationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ----------retrieve ViewBinding
        binding = ActivityExaminationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --------retrieve variables
        val nDismissedImages: Int? =
            nDismissedImagesRetriever(intent, IntentIdentifiers.N_DISMISSED_IMAGES, 0)
        val conductAutoScroll: Boolean =
            UserPreferences.conductAutoScrolling && cropBundles.size > 1

        //----------retrieve ViewModel
        viewModel = ViewModelProvider(
            this,
            ExaminationViewModelFactory(
                conductAutoScroll,
                longAutoScrollDelay = nDismissedImages.notNull()
            )
        )[ExaminationViewModel::class.java]

        // --------commit ExaminationFragment if applicable
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(binding.layout.id, ViewPagerFragment())
                .commit()
        }

        // ---------display Snackbars
        if (nDismissedImages.notNull())
            displaySnackbar(
                "Couldn't find cropping bounds for\n$nDismissedImages image${numberInflection(nDismissedImages!!)}",
                TextColors.URGENT
            )
        else if (conductAutoScroll)
            displaySnackbar(
                "Tap screen to cancel auto scrolling",
                TextColors.NEUTRAL,
                Snackbar.LENGTH_SHORT
            )
    }

    val saveAllFragment: SaveAllFragment by lazy { SaveAllFragment() }
    val appTitleFragment: AppTitleFragment by lazy { AppTitleFragment() }

    fun Fragment.commit(flipRight: Boolean){
        val animations = arrayOf(
            arrayOf(
                R.animator.card_flip_left_in,
                R.animator.card_flip_left_out
            ),
            arrayOf(
                R.animator.card_flip_right_in,
                R.animator.card_flip_right_out
            )
        )[flipRight]

        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(animations[0], animations[1])
            .replace(binding.layout.id, this)
            .commit()
    }

    /**
     * Block backPress throughout the process of saving all crops,
     * otherwise return to MainActivity after confirmation
     */
    private val handleBackPress = BackPressHandler(this, "Tap again to return to main screen"){
        returnToMainActivity()
    }

    override fun onBackPressed() = when {
        appTitleFragment.isVisible -> Unit
        saveAllFragment.isVisible -> {
            displaySnackbar(
                "Wait until crops have been saved",
                TextColors.URGENT
            )
        }
        else -> handleBackPress()
    }

    /**
     * Clears remaining cropBundle elements contained within cropBundleList
     */
    fun returnToMainActivity() = logAfterwards("Cleared cropBundleList") {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).putExtra(IntentIdentifiers.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS, intArrayOf(viewModel.nSavedCrops, viewModel.nDeletedCrops))
        )

        returnTransitionAnimation()
    }

    override fun onStop() {
        super.onStop()

        cropBundles.clear()
        finishAndRemoveTask()
    }
}