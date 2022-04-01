package com.autocrop.activities.examination.fragments.singleaction.saveall

import com.autocrop.UserPreferences
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.singleaction.SingleActionExaminationActivityFragment
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentSaveallBinding


class SaveAllFragment :
    SingleActionExaminationActivityFragment<ActivityExaminationFragmentSaveallBinding>(ActivityExaminationFragmentSaveallBinding::inflate) {

    /**
     * Save all remaining crops and delete corresponding screenshots if applicable,
     * increment respective counters within [sharedViewModel]
     * invoke AppTitleFragment
     */
    override fun runAction() {
        CropSaver(UserPreferences.deleteScreenshotsOnSaveAll, activity.contentResolver) {
            sharedViewModel.incrementImageFileIOCounters(ExaminationActivity.cropBundles.size, UserPreferences.deleteScreenshotsOnSaveAll)

            with(activity){
                replaceCurrentFragmentWith(appTitleFragment, true)
            }
        }
            .execute()
    }
}