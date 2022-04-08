package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import android.view.View
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentSaveallBinding


class SaveAllFragment :
    ExaminationActivityFragment<ActivityExaminationFragmentSaveallBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CropSaver(BooleanUserPreferences.deleteScreenshotsOnSaveAll, typedActivity.contentResolver, sharedViewModel::incrementImageFileIOCounters) {
            with(typedActivity){
                replaceCurrentFragmentWith(appTitleFragment, true)
            }
        }
            .execute()
    }
}