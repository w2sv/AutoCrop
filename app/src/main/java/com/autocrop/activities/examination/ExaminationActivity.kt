/**
 * ViewPager2 Reference: https://medium.com/swlh/android-infinite-auto-image-slider-using-view-pager-2-android-studio-java-a0e450dec071
 */

package com.autocrop.activities.examination

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.autocrop.CropBundle
import com.autocrop.activities.BackPressHandler
import com.autocrop.activities.FragmentHostingActivity
import com.autocrop.activities.IntentIdentifiers
import com.autocrop.activities.examination.fragments.singleaction.apptitle.AppTitleFragment
import com.autocrop.activities.examination.fragments.singleaction.saveall.SaveAllFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.main.MainActivity
import com.autocrop.utils.android.*
import com.autocrop.utils.get
import com.autocrop.utils.logAfterwards
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationBinding

class ExaminationActivity : FragmentHostingActivity<ActivityExaminationBinding>(ActivityExaminationBinding::inflate) {

    companion object{
        lateinit var cropBundles: MutableList<CropBundle>
    }

    lateinit var viewModel: ExaminationViewModel

    private val nDismissedImagesRetriever = IntentExtraRetriever<Int>()

    override val rootFragment: ViewPagerFragment by lazy{ViewPagerFragment()}
    val saveAllFragment: SaveAllFragment by lazy { SaveAllFragment() }
    val appTitleFragment: AppTitleFragment by lazy { AppTitleFragment() }

    override fun onCreateCore() {
        fragmentContainerViewId = binding.layout.id

        //----------retrieve ViewModel
        viewModel = ViewModelProvider(
            this,
            ExaminationViewModelFactory(nDismissedImages = nDismissedImagesRetriever(intent, IntentIdentifiers.N_DISMISSED_IMAGES) ?: 0)
        )[ExaminationViewModel::class.java]

        // ---------display Snackbar
        with(viewModel.nDismissedImages) {
            if (!equals(0))
                displaySnackbar(
                    "Couldn't find cropping bounds for\n$this image${numberInflection(this)}",
                    TextColors.URGENT,
                    1800
                )
        }
    }

    fun replaceCurrentFragmentWith(fragment: Fragment, flipRight: Boolean) {
        super.replaceCurrentFragmentWith(
            fragment,
            arrayOf(
                R.animator.card_flip_left_in to R.animator.card_flip_left_out,
                R.animator.card_flip_right_in to R.animator.card_flip_right_out
            )[flipRight]
        )
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
    fun returnToMainActivity(){
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).putExtra(
                IntentIdentifiers.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS,
                intArrayOf(
                    viewModel.nSavedCrops,
                    viewModel.nDeletedCrops
                )
            )
        )
        returnTransitionAnimation()
    }

    override fun onStop() = logAfterwards("Cleared cropBundles") {
        super.onStop()

        cropBundles.clear()
        finishAndRemoveTask()
    }
}