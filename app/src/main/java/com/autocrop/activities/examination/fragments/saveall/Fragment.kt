package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.cropBundleList
import com.autocrop.utils.android.show
import com.bunsenbrenner.screenshotboundremoval.R
import java.lang.ref.WeakReference


class SaveAllFragment: ExaminationActivityFragment(R.layout.activity_examination_saveall) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveAll()
    }

    private fun saveAll() {
        activity.nSavedCrops += cropBundleList.size

        CropSaver(
            WeakReference(activity),
            onTaskFinished = {activity.invokeAppTitleFragment(false)}
        )
            .execute()
    }
}