package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.processCropBundle
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.utils.executeAsyncTask
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentSaveallBinding

class SaveAllFragment :
    ExaminationActivityFragment<ActivityExaminationFragmentSaveallBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.executeAsyncTask(
            { processRemainingCropBundles(BooleanUserPreferences.deleteScreenshotsOnSaveAll)},
            { typedActivity.redirectToExitFragment() }
        )
    }

    private fun processRemainingCropBundles(deleteCorrespondingScreenshots: Boolean): Void? {
        ExaminationActivityViewModel.cropBundles.forEach { bundle ->
            val (_, deletionResult) = requireContext().processCropBundle(
                bundle,
                deleteCorrespondingScreenshots,
                sharedViewModel.documentUriWritePermissionValid
            )
            deletionResult?.let { (deletionUri, _) ->
                if (deletionUri != null)
                    sharedViewModel.deletionQueryScreenshotUris.add(deletionUri)
            }

            sharedViewModel.incrementImageFileIOCounters(deleteCorrespondingScreenshots)
        }
        return null
    }
}