package com.autocrop.activities.examination.fragments.viewpager

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.w2sv.autocrop.databinding.ExaminationFragmentViewpagerBinding

class ViewPagerFragment:
    ExaminationActivityFragment<ExaminationFragmentViewpagerBinding>(){

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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(CURRENT_VIEW_PAGER_POSITION, binding.viewPager.currentItem)
    }
}
