package com.w2sv.autocrop.activities.cropping.fragments.cropping

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.autocrop.CropBundle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.Screenshot
import com.w2sv.autocrop.activities.cropping.CropActivity
import com.w2sv.autocrop.activities.cropping.cropping.cropEdgesCandidates
import com.w2sv.autocrop.activities.cropping.cropping.maxHeightEdges
import com.w2sv.autocrop.activities.cropping.fragments.CropActivityFragment
import com.w2sv.autocrop.activities.cropping.fragments.croppingfailed.CroppingFailedFragment
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivity
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.w2sv.autocrop.databinding.FragmentCropBinding
import com.w2sv.autocrop.utils.android.BackPressListener
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.extensions.loadBitmap
import com.w2sv.autocrop.utils.android.extensions.snackyBuilder
import com.w2sv.kotlinutils.extensions.executeAsyncTask
import com.w2sv.kotlinutils.extensions.launchDelayed
import kotlinx.coroutines.Job

class CropFragment
    : CropActivityFragment<FragmentCropBinding>(FragmentCropBinding::class.java) {

    class ViewModel: androidx.lifecycle.ViewModel(){
        val backPressListener = BackPressListener(viewModelScope)
    }

    private val viewModel by viewModels<ViewModel>()

    fun onBackPress(){
        viewModel.backPressListener(
            {
                requireActivity()
                    .snackyBuilder("Tap again to cancel")
                    .build()
                    .show()
            },
            {
                castActivity.startMainActivity()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.liveCropBundles.observe(activity as LifecycleOwner) {
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

    private fun cropImages(){
        sharedViewModel.imminentUris.forEach { uri ->
            try {
                // attempt to crop image; upon success add CropBundle to sharedViewModel
                val bitmap = requireContext().contentResolver.loadBitmap(uri)

                bitmap.cropEdgesCandidates()?.let { candidates ->
                    sharedViewModel.liveCropBundles.add(
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
            } catch (_: IllegalStateException){}
        }
    }

    private fun proceed() {
        if (sharedViewModel.liveCropBundles.isNotEmpty())
            startIODeterminationActivity()
        else
            lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_small)) {
                // delay briefly to assure progress bar having reached 100% before UI change
                fragmentHostingActivity
                    .fragmentReplacementTransaction(CroppingFailedFragment())
                    .commit()
            }
    }

    /**
     * Inherently sets [IODeterminationActivityViewModel.cropBundles]
     */
    private fun startIODeterminationActivity() {
        IODeterminationActivityViewModel.cropBundles = sharedViewModel.liveCropBundles

        requireActivity().let {
            startActivity(
                Intent(it, IODeterminationActivity::class.java).putExtra(
                    CropActivity.EXTRA_N_UNCROPPED_IMAGES,
                    sharedViewModel.nUncroppedImages
                )
            )
            Animatoo.animateSwipeLeft(it)
        }
    }

    override fun onPause() {
        super.onPause()

        cropJob.cancel()
    }
}