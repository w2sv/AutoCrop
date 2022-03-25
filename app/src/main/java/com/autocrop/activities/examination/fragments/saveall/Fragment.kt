package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.autocrop.activities.examination.ExaminationViewModel
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.cropBundleList
import com.w2sv.autocrop.R
import java.lang.ref.WeakReference


class SaveAllFragment : ExaminationActivityFragment(R.layout.activity_examination_fragment_saveall) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveAll()
    }

    private fun saveAll() {
        CropSaver(WeakReference(activity)) {
            viewModel.incrementNSavedCrops(cropBundleList.size)
            with(activity){appTitleFragment.commit(true)}
        }
            .execute()
    }
}