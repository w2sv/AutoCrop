package com.autocrop.activities.examination.fragments.singleaction.saveall

import com.autocrop.UserPreferences
import com.autocrop.activities.examination.fragments.singleaction.SingleActionExaminationActivityFragment
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentSaveallBinding


class SaveAllFragment :
    SingleActionExaminationActivityFragment<ActivityExaminationFragmentSaveallBinding>(ActivityExaminationFragmentSaveallBinding::inflate) {

    /**
     * Save all remaining crops and delete corresponding screenshots if applicable,
     * increment respective counters within [viewModel]
     * invoke AppTitleFragment
     */
    override fun runAction() {
        CropSaver(UserPreferences.deleteScreenshotsOnSaveAll, activity.contentResolver) {

            viewModel.nSavedCrops += viewModel.viewPager.dataSet.size
            if (UserPreferences.deleteScreenshotsOnSaveAll)
                viewModel.nDeletedCrops += viewModel.viewPager.dataSet.size

            with(activity){
                replaceCurrentFragmentWith(appTitleFragment, true)
            }
        }
            .execute()
    }
}