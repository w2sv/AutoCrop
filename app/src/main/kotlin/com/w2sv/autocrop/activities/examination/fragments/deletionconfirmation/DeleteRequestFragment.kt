@file:SuppressLint("NewApi")

package com.w2sv.autocrop.activities.examination.fragments.deletionconfirmation

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
import com.w2sv.androidutils.ActivityCallContractAdministrator
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.w2sv.autocrop.databinding.FragmentDeleteRequestBinding
import com.w2sv.autocrop.utils.SimpleAnimationListener

class DeleteRequestFragment :
    ApplicationFragment<FragmentDeleteRequestBinding>(FragmentDeleteRequestBinding::class.java) {

    private class DeleteRequestIntentContractAdministrator(
        activity: ComponentActivity,
        override val activityResultCallback: (ActivityResult) -> Unit
    ) : ActivityCallContractAdministrator.Impl<IntentSenderRequest, ActivityResult>(
        activity,
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        fun emitDeleteRequest(contentResolver: ContentResolver, deletionInquiryUris: ArrayList<Uri>) {
            activityResultLauncher.launch(
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

    private val activityViewModel by activityViewModels<ExaminationActivity.ViewModel>()

    private val deleteRequestIntentContractAdministrator by lazy {
        DeleteRequestIntentContractAdministrator(
            requireActivity()
        ) {
            if (it.resultCode == Activity.RESULT_OK)
                activityViewModel.onDeleteRequestUrisDeleted()

            launchAfterShortDelay {  // necessary for showing of transition animation, which otherwise is just skipped
                getFragmentHostingActivity()
                    .fragmentReplacementTransaction(
                        AppTitleFragment(),
                        true
                    )
                    .commit()
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