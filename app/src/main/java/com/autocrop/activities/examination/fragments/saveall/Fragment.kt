package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import android.view.View
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.cropBundleList
import com.bunsenbrenner.screenshotboundremoval.R
import java.lang.ref.WeakReference


class SaveAllFragment : ExaminationActivityFragment(R.layout.activity_examination_saveall) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveAll()
    }

    private fun saveAll() {
        activity.nSavedCrops += cropBundleList.size

        CropSaver(
            WeakReference(activity),
            onTaskFinished = { activity.invokeAppTitleFragment(false) }
        )
            .execute()
    }
}