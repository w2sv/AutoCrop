package com.w2sv.autocrop.activities.crop.fragments.cropping

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.autocrop.cropping.cropbundle.CropBundle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.cropping.cropbundle.Screenshot
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.crop.CropActivityViewModel
import com.w2sv.autocrop.cropping.cropEdgesCandidates
import com.w2sv.autocrop.cropping.maxHeightEdges
import com.w2sv.autocrop.activities.crop.fragments.croppingfailed.CroppingFailedFragment
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivityViewModel
import com.w2sv.autocrop.controller.ApplicationFragment
import com.w2sv.autocrop.databinding.FragmentCropBinding
import com.w2sv.autocrop.utils.android.BackPressListener
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.extensions.loadBitmap
import com.w2sv.autocrop.utils.android.extensions.snackyBuilder
import com.w2sv.kotlinutils.extensions.executeAsyncTask
import com.w2sv.kotlinutils.extensions.launchDelayed
import kotlinx.coroutines.Job

class CropFragment
    : ApplicationFragment<FragmentCropBinding>(FragmentCropBinding::class.java) {

    class ViewModel : androidx.lifecycle.ViewModel() {
        val backPressListener = BackPressListener(viewModelScope)
    }

    private val viewModel by viewModels<ViewModel>()
    private val activityViewModel by activityViewModels<CropActivityViewModel>()

    fun onBackPress() {
        viewModel.backPressListener(
            {
                requireActivity()
                    .snackyBuilder("Tap again to cancel")
                    .build()
                    .show()
            },
            {
                castActivity<CropActivity>().startMainActivity()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.liveCropBundles.observe(activity as LifecycleOwner) {
            binding.progressTv.update(it.size)
            binding.croppingProgressBar.progress = it.size
        }
    }

    private lateinit var cropJob: Job

    override fun onResume() {
        super.onResume()

        cropJob = lifecycleScope.executeAsyncTask(
            { cropImages() },
            { proceed() }
        )
    }

    private fun cropImages() {
        activityViewModel.imminentUris.forEach { uri ->
            try {
                // attempt to crop image; upon success add CropBundle to sharedViewModel
                val bitmap = requireContext().contentResolver.loadBitmap(uri)

                bitmap.cropEdgesCandidates()?.let { candidates ->
                    activityViewModel.liveCropBundles.add(
                        CropBundle.assemble(
                            Screenshot(
                                uri,
                                bitmap.height,
                                candidates,
                                Screenshot.MediaStoreData.query(requireContext().contentResolver, uri)
                            ),
                            bitmap,
                            candidates.maxHeightEdges()
                        )
                    )
                }
            }
            catch (_: IllegalStateException) {
            }
        }
    }

    private fun proceed() {
        if (activityViewModel.liveCropBundles.isNotEmpty())
            startIODeterminationActivity()
        else
            lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_small)) {
                // delay briefly to assure progress bar having reached 100% before UI change
                fragmentHostingActivity
                    .fragmentReplacementTransaction(CroppingFailedFragment())
                    .commit()
            }
    }

    override fun onPause() {
        super.onPause()

        cropJob.cancel()
    }

    /**
     * Inherently sets [CropExaminationActivityViewModel.cropBundles]
     */
    private fun startIODeterminationActivity() {
        CropExaminationActivityViewModel.cropBundles = activityViewModel.liveCropBundles

        requireActivity().let {
            startActivity(
                Intent(it, CropExaminationActivity::class.java).putExtra(
                    CropActivity.EXTRA_N_UNCROPPED_IMAGES,
                    activityViewModel.nUncroppedImages
                )
            )
            Animatoo.animateSwipeLeft(it)
        }
    }
}