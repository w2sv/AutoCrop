package com.autocrop.activities.examination.fragments.viewpager

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.uicontroller.fragment.ActivityRootFragment
import com.autocrop.utils.android.IntentExtraRetriever
import com.autocrop.utils.android.displaySnackbar
import com.autocrop.utils.android.getColorInt
import com.autocrop.utils.numericallyInflected
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ExaminationFragmentViewpagerBinding

class ViewPagerFragment:
    ExaminationActivityFragment<ExaminationFragmentViewpagerBinding>(),
    ActivityRootFragment {

    companion object{
        private const val CURRENT_VIEW_PAGER_POSITION = "CURRENT_VIEW_PAGER_POSITION"
    }

    private lateinit var viewPagerHandler: ViewPagerHandler
    private lateinit var viewModel: ViewPagerViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ViewPagerViewModel::class.java]

        viewPagerHandler = ViewPagerHandler(
            binding,
            viewModel,
            typedActivity,
            savedInstanceState?.getInt(CURRENT_VIEW_PAGER_POSITION)
        )

        if (savedInstanceState == null)
            displayActivityEntrySnackbar()
    }

    private val nDismissedImagesRetriever = IntentExtraRetriever<Int>(IntentExtraIdentifier.N_DISMISSED_IMAGES)

    override fun displayActivityEntrySnackbar(){
        nDismissedImagesRetriever(requireActivity().intent, 0) ?.let {
            requireActivity().displaySnackbar(
                SpannableStringBuilder()
                    .append("Couldn't find cropping bounds for ")
                    .bold {
                        color(
                            getColorInt(R.color.magenta_saturated, requireContext())
                        ) { append("$it") }
                    }
                    .append(" image".numericallyInflected(it)),
                R.drawable.ic_error_24
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(CURRENT_VIEW_PAGER_POSITION, binding.viewPager.currentItem)
    }
}
