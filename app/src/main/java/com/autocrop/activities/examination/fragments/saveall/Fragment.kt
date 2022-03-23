package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.activities.examination.fragments.DownstreamExaminationActivityFragment
import com.autocrop.cropBundleList
import com.w2sv.autocrop.R
import java.lang.ref.WeakReference


class SaveAllFragment : DownstreamExaminationActivityFragment(R.layout.activity_examination_saveall) {
    private val viewModel: ExaminationViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveAll()
    }

    private fun saveAll() {
        CropSaver(WeakReference(examinationActivity)) {
            viewModel.incrementNSavedCrops(cropBundleList.size)
            examinationActivity.appTitleFragment.value.invoke(true)
        }
            .execute()
    }
}