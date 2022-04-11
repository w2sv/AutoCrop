package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.utils.executeAsyncTask
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentSaveallBinding

class SaveAllFragment :
    ExaminationActivityFragment<ActivityExaminationFragmentSaveallBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.executeAsyncTask(
            { processRemainingCropBundles(BooleanUserPreferences.deleteScreenshotsOnSaveAll) },
            ::invokeAppTitleFragment
        )
    }

    private fun processRemainingCropBundles(deleteCorrespondingScreenshots: Boolean): Void? {
        ExaminationActivityViewModel.cropBundles.forEachIndexed { i, bundle ->
            val writeUri = saveCropAndDeleteScreenshotIfApplicable(
                bundle.screenshotUri,
                bundle.crop,
                deleteCorrespondingScreenshots,
                requireContext().contentResolver
            )
            sharedViewModel.incrementImageFileIOCounters(deleteCorrespondingScreenshots)
            if (i == 0)
                sharedViewModel.setCropWriteDirPathIfApplicable(writeUri)
        }
        return null
    }

    private fun invokeAppTitleFragment(it: Void?) =
        with(typedActivity){
            replaceCurrentFragmentWith(appTitleFragment, true)
        }
}