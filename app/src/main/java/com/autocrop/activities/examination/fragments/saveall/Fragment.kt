package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import android.view.View
import com.autocrop.activities.examination.fragments.DownstreamExaminationActivityFragment
import com.autocrop.cropBundleList
import com.w2sv.autocrop.R
import java.lang.ref.WeakReference


class SaveAllFragment : DownstreamExaminationActivityFragment(R.layout.activity_examination_saveall) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveAll()
    }

    private fun saveAll() {
        CropSaver(
            WeakReference(examinationActivity),
            onTaskFinished = {
                with(examinationActivity){
                    nSavedCrops += cropBundleList.size
                    appTitleFragment.value.invoke(false, supportFragmentManager)
                }
            }
        )
            .execute()
    }
}