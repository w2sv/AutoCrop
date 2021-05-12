package com.autocrop.activities.examination.fragments.aftermath

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.cropBundleList
import com.autocrop.utils.android.show
import com.bunsenbrenner.screenshotboundremoval.R
import java.lang.ref.WeakReference


class AftermathFragment(private val displaySaveAllScreen: Boolean) : ExaminationActivityFragment() {
    inner class TextViews {
        val appTitle: TextView = findViewById(R.id.examination_activity_title_text_view)
        val saveAll: TextView = findViewById(R.id.processing_crops_text_view)
    }

    lateinit var textViews: TextViews
    val displayingSaveAllScreen: Boolean
        get() = textViews.saveAll.isVisible

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.activity_examination_back, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViews = TextViews()

        if (displaySaveAllScreen)
            saveAll()
        else
            exitActivity()
    }

    private fun conductDelayedReturnToMainActivity(){
        Handler().postDelayed(
            { activity.returnToMainActivity() },
            1000
        )
    }

    private fun saveAll() {
        activity.nSavedCrops += cropBundleList.size

        CropSaver(
            WeakReference(findViewById(R.id.indeterminateBar)),
            WeakReference(textViews),
            WeakReference(activity),
            onTaskFinished = this::conductDelayedReturnToMainActivity
        )
            .execute()
    }

    private fun exitActivity() {
        textViews.appTitle.show()
        conductDelayedReturnToMainActivity()
    }
}