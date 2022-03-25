package com.autocrop.activities.examination.fragments.singleaction.saveall

import com.autocrop.UserPreferences
import com.autocrop.activities.examination.fragments.singleaction.SingleActionExaminationActivityFragment
import com.autocrop.cropBundleList
import com.w2sv.autocrop.R
import java.lang.ref.WeakReference


class SaveAllFragment :
    SingleActionExaminationActivityFragment(R.layout.activity_examination_fragment_saveall) {

    override fun runAction() {
        CropSaver(UserPreferences.deleteScreenshotsOnSaveAll, WeakReference(activity)) {
            viewModel.incrementNSavedCrops(cropBundleList.size)
            with(activity){appTitleFragment.commit(true)}
        }
            .execute()
    }
}