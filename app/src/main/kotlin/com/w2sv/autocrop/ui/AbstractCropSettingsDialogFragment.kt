package com.w2sv.autocrop.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.slider.Slider
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.views.RoundedDialogFragment
import com.w2sv.cropbundle.cropping.CROP_SENSITIVITY_MAX
import com.w2sv.cropbundle.cropping.CropSensitivity

abstract class AbstractCropSettingsDialogFragment(
    @StringRes private val title: Int,
    @DrawableRes private val icon: Int,
    @StringRes private val positiveButtonText: Int
) : RoundedDialogFragment() {

    abstract class ViewModel(@CropSensitivity private val initialCropSensitivity: Int) : androidx.lifecycle.ViewModel() {

        val cropSensitivity: LiveData<Int> get() = _cropSensitivity
        private val _cropSensitivity = MutableLiveData(initialCropSensitivity)

        fun setCropSensitivity(@CropSensitivity value: Int) {
            _cropSensitivity.postValue(value)
            _sensitivityHasChanged.postValue(value != initialCropSensitivity)
        }

        val sensitivityHasChanged: LiveData<Boolean> get() = _sensitivityHasChanged
        private val _sensitivityHasChanged = MutableLiveData(false)
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
            findViewById<Slider>(R.id.sensitivity_slider)!!
                .apply {
                    valueFrom = 0f
                    valueTo = CROP_SENSITIVITY_MAX.toFloat()
                    stepSize = 1f
                    value = viewModel.cropSensitivity.value!!.toFloat()

                    addOnChangeListener { _, value, fromUser ->
                        if (fromUser) {
                            viewModel.setCropSensitivity(value.toInt())
                        }
                    }
                }
        }
    }

    private fun ViewModel.setLiveDataObservers(dialog: AlertDialog) {
        sensitivityHasChanged.observe(requireParentFragment().viewLifecycleOwner) { settingsChanged ->
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .apply {
                    isEnabled = settingsChanged
                }
        }
    }
}