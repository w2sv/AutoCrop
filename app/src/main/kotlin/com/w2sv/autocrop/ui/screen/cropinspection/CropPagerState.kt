package com.w2sv.autocrop.ui.screen.cropinspection

import android.view.View
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras

@Stable
data class CropPagerState(val pagerState: PagerState, private val getCropTransitionName: (Int) -> String) {
    val currentPage: Int by pagerState::currentPage
    val pageCount: Int by pagerState::pageCount

    private val transitionNameToImageView: MutableMap<String, View> = mutableMapOf()

    var exitAnimationPage by mutableStateOf<Int?>(null)
        private set

    val pageIndication by derivedStateOf { "${currentPage + if (pageCount > 0) 1 else 0}/${pageCount}" }

    fun launchExitAnimationForCurrentPage() {
        exitAnimationPage = currentPage
    }

    fun addTransitionView(transitionName: String, view: View) {
        transitionNameToImageView[transitionName] = view
    }

    fun removeTransitionView(transitionName: String) {
        transitionNameToImageView.remove(transitionName)
    }

    fun navigatorExtras(): FragmentNavigator.Extras {
        val transitionName = getCropTransitionName(currentPage)
        return FragmentNavigatorExtras(transitionNameToImageView.getValue(transitionName) to transitionName)
    }
}

@Composable
fun rememberCropPagerState(pageCount: Int, getCropTransitionName: (Int) -> String): CropPagerState {
    val pagerState = rememberPagerState { pageCount }
    return remember(pageCount) { CropPagerState(pagerState, getCropTransitionName) }
}
