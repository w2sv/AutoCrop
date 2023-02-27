package com.w2sv.autocrop.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.SeekBar
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.model.CROP_SENSITIVITY_MAX
import com.w2sv.autocrop.ui.model.cropSensitivity
import com.w2sv.autocrop.ui.model.edgeCandidateThreshold

abstract class CropSettingsDialog(
    @StringRes private val title: Int,
    @StringRes private val positiveButtonText: Int
) : DialogFragment() {

    abstract class ViewModel(initialEdgeCandidateThreshold: Int) : androidx.lifecycle.ViewModel() {
        @IntRange(from = 0, to = 20)
        private val initialCropSensitivity: Int = cropSensitivity(initialEdgeCandidateThreshold)

        val cropSensitivityLive: LiveData<Int> by lazy {
            MutableLiveData(initialCropSensitivity)
        }

        val edgeCandidateThreshold: Int get() = edgeCandidateThreshold(cropSensitivityLive.value!!)

        val settingsDissimilarLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }

        fun onSettingsInput() {
            settingsDissimilarLive.postValue(cropSensitivityLive.value != initialCropSensitivity)
        }
    }

    abstract val viewModel: ViewModel

    abstract fun onPositiveButtonClicked()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(title))
            .setView(R.layout.crop_settings)
            .setPositiveButton(positiveButtonText) { _, _ ->
                onPositiveButtonClicked()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .create()
            .apply {
                setOnShowListener {
                    viewModel.setLiveDataObservers(this)
                    findViewById<SeekBar>(R.id.sensitivity_seekbar)!!.set()
                }
            }

    private fun ViewModel.setLiveDataObservers(dialog: AlertDialog) {
        cropSensitivityLive.observe(requireParentFragment().viewLifecycleOwner) {
            onSettingsInput()
        }
        settingsDissimilarLive.observe(requireParentFragment().viewLifecycleOwner) { settingsChanged ->
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                isEnabled = settingsChanged
            }
        }
    }

    private fun SeekBar.set() {
        progress = viewModel.cropSensitivityLive.value!!
        min = 0
        max = CROP_SENSITIVITY_MAX

        setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        viewModel.cropSensitivityLive.postValue(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
    }
}