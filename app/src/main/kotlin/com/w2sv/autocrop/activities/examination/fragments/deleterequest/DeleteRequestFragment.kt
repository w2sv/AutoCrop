@file:SuppressLint("NewApi")

package com.w2sv.autocrop.activities.examination.fragments.deleterequest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.w2sv.androidutils.ActivityCallContractHandler
import com.w2sv.androidutils.ui.SimpleAnimationListener
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.common.databinding.DeleteRequestBinding

class DeleteRequestFragment :
    AppFragment<DeleteRequestBinding>(DeleteRequestBinding::class.java) {

    private val activityViewModel by activityViewModels<ExaminationActivity.ViewModel>()

    private val deleteRequestIntentContractAdministrator by lazy {
        DeleteRequestIntentContractAdministrator(
            requireActivity()
        ) {
            if (it.resultCode == Activity.RESULT_OK)
                activityViewModel.onDeleteRequestUrisDeleted()

            launchAfterShortDelay {  // necessary for showing of transition animation, which otherwise is just skipped
                castActivity<ExaminationActivity>().invokeSubsequentController(this@DeleteRequestFragment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(deleteRequestIntentContractAdministrator)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? =
        if (enter)
            AnimationUtils.loadAnimation(requireActivity(), nextAnim)
                .apply {
                    setAnimationListener(
                        object : SimpleAnimationListener() {
                            override fun onAnimationEnd(animation: Animation?) {
                                deleteRequestIntentContractAdministrator.emitDeleteRequest(
                                    requireContext().contentResolver,
                                    activityViewModel.deleteRequestUris
                                )
                            }
                        }
                    )
                }
        else
            super.onCreateAnimation(transit, false, nextAnim)
}

private class DeleteRequestIntentContractAdministrator(
    activity: ComponentActivity,
    override val resultCallback: (ActivityResult) -> Unit
) : ActivityCallContractHandler.Impl<IntentSenderRequest, ActivityResult>(
    activity,
    ActivityResultContracts.StartIntentSenderForResult()
) {
    fun emitDeleteRequest(contentResolver: ContentResolver, deletionInquiryUris: ArrayList<Uri>) {
        resultLauncher.launch(
            IntentSenderRequest.Builder(
                MediaStore.createDeleteRequest(
                    contentResolver,
                    deletionInquiryUris
                )
                    .intentSender
            )
                .build()
        )
    }
}