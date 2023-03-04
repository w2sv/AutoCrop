package com.w2sv.autocrop.ui

import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.slider.Slider
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.model.CROP_SENSITIVITY_MAX
import com.w2sv.autocrop.ui.model.cropSensitivity
import com.w2sv.autocrop.ui.model.edgeCandidateThreshold
import com.w2sv.autocrop.ui.views.RoundedDialogFragment

abstract class AbstractCropSettingsDialogFragment(
    @StringRes private val title: Int,
    @DrawableRes private val icon: Int,
    @StringRes private val positiveButtonText: Int
) : RoundedDialogFragment() {

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

    override fun AlertDialog.Builder.build(): AlertDialog.Builder =
        this
            .setIcon(icon)
            .setTitle(resources.getString(title))
            .setView(R.layout.crop_settings)
            .setPositiveButton(positiveButtonText) { _, _ ->
                onPositiveButtonClicked()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }

    override fun AlertDialog.onCreatedListener() {
        setOnShowListener {
            viewModel.setLiveDataObservers(this)
            findViewById<Slider>(R.id.sensitivity_slider)!!.set()
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

    private fun Slider.set() {
        valueFrom = 0f
        valueTo = CROP_SENSITIVITY_MAX.toFloat()
        stepSize = 1f
        value = viewModel.cropSensitivityLive.value!!.toFloat()

        addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.cropSensitivityLive.postValue(value.toInt())
            }
        }
    }
}