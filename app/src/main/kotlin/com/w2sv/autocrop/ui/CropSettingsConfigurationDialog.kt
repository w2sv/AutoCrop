package com.w2sv.autocrop.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.text.buildSpannedString
import androidx.core.text.italic
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.cropbundle.CropBundle
import com.w2sv.preferences.IntPreferences
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

abstract class CropSettingsDialog(
    @StringRes private val title: Int,
    @StringRes private val positiveButtonText: Int
) : DialogFragment() {

    abstract class ViewModel(protected val intPreferences: IntPreferences) : androidx.lifecycle.ViewModel() {
        val cropEdgeCandidateThresholdLive: LiveData<Int> by lazy {
            MutableLiveData(intPreferences.cropEdgeCandidateThreshold)
        }

        val settingsDissimilarLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }

        fun onSettingsInput() {
            settingsDissimilarLive.postValue(cropEdgeCandidateThresholdLive.value != intPreferences.cropEdgeCandidateThreshold)
        }
    }

    abstract val viewModel: ViewModel

    abstract fun onPositiveButtonClicked()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(title))
            .setView(R.layout.crop_settings_configuration)
            .setPositiveButton(positiveButtonText) { _, _ ->
                onPositiveButtonClicked()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .create()
            .apply {
                setOnShowListener {
                    viewModel.settingsDissimilarLive.observe(requireParentFragment().viewLifecycleOwner) { settingsChanged ->
                        getButton(AlertDialog.BUTTON_POSITIVE).apply {
                            isEnabled = settingsChanged
                        }
                    }

                    viewModel.cropEdgeCandidateThresholdLive.observe(requireParentFragment().viewLifecycleOwner) { threshold ->
                        findViewById<TextView>(R.id.threshold_tv)!!.text = buildSpannedString {
                            append("Threshold: ")
                            italic {
                                append(threshold.toString())
                            }
                        }
                        viewModel.onSettingsInput()
                    }

                    findViewById<SeekBar>(R.id.threshold_seekbar)!!
                        .apply {
                            progress = viewModel.cropEdgeCandidateThresholdLive.value!!

                            setOnSeekBarChangeListener(
                                object : SeekBar.OnSeekBarChangeListener {
                                    override fun onProgressChanged(
                                        seekBar: SeekBar?,
                                        progress: Int,
                                        fromUser: Boolean
                                    ) {
                                        if (fromUser) {
                                            viewModel.cropEdgeCandidateThresholdLive.postValue(progress)
                                        }
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                                }
                            )
                        }
                }
            }
}

@AndroidEntryPoint
class CropSettingsConfigurationDialog : CropSettingsDialog(R.string.crop_settings, R.string.apply) {

    @HiltViewModel
    class ViewModel @Inject constructor(intPreferences: IntPreferences) : CropSettingsDialog.ViewModel(intPreferences) {

        fun syncCropSettings() {
            intPreferences.cropEdgeCandidateThreshold = cropEdgeCandidateThresholdLive.value!!
            settingsDissimilarLive.postValue(false)
        }
    }

    override val viewModel by viewModels<ViewModel>(ownerProducer = { requireParentFragment() })

    override fun onPositiveButtonClicked() {
        viewModel.syncCropSettings()
        requireContext().showToast("Updated Crop Settings!")
    }
}

@AndroidEntryPoint
class RecropDialog : CropSettingsDialog(R.string.recrop_with_different_settings, R.string.recrop) {

    companion object {
        fun getInstance(cropBundlePosition: Int): RecropDialog =
            getFragment(RecropDialog::class.java, CropBundle.EXTRA_POSITION to cropBundlePosition)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        intPreferences: IntPreferences,
        savedStateHandle: SavedStateHandle
    ) : CropSettingsDialog.ViewModel(intPreferences) {
        val cropBundlePosition: Int = savedStateHandle[CropBundle.EXTRA_POSITION]!!
    }

    override val viewModel by viewModels<ViewModel>()

    override fun onPositiveButtonClicked() {
        (requireParentFragment() as Listener).onDoRecrop(
            viewModel.cropBundlePosition,
            viewModel.cropEdgeCandidateThresholdLive.value!!.toDouble()
        )
    }

    interface Listener {
        fun onDoRecrop(cropBundlePosition: Int, threshold: Double)
    }
}