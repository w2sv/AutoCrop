package com.autocrop.activities.examination.fragments.singleaction.saveall

import com.autocrop.UserPreferences
import com.autocrop.activities.examination.fragments.singleaction.SingleActionExaminationActivityFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentSaveallBinding
import java.lang.ref.WeakReference


class SaveAllFragment :
    SingleActionExaminationActivityFragment<ActivityExaminationFragmentSaveallBinding>(ActivityExaminationFragmentSaveallBinding::inflate) {

    override fun runAction() {
        CropSaver(UserPreferences.deleteScreenshotsOnSaveAll, WeakReference(activity)) {
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