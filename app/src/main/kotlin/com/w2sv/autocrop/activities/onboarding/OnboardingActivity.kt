package com.w2sv.autocrop.activities.onboarding

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.viewModelScope
import com.airbnb.lottie.LottieAnimationView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.extensions.crossVisualize
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.androidutils.permissionhandler.requestPermissions
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.views.animationComposer
import com.w2sv.autocrop.utils.extensions.addObservers
import com.w2sv.autocrop.utils.extensions.registerOnBackPressedListener
import com.w2sv.autocrop.utils.extensions.startMainActivity
import com.w2sv.common.BackPressHandler
import com.w2sv.common.preferences.GlobalFlags
import com.w2sv.onboarding.OnboardingPage
import com.w2sv.screenshotlistening.ScreenshotListener
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : com.w2sv.onboarding.OnboardingActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(val globalFlags: GlobalFlags) : androidx.lifecycle.ViewModel() {
        var screenshotListeningEnabled: Boolean = false

        val backPressHandler = BackPressHandler(viewModelScope, 2500L)
    }

    private val viewModel by viewModels<ViewModel>()

    private val screenshotListeningPermissionHandlers by lazy {
        ScreenshotListener.permissionHandlers(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addObservers(screenshotListeningPermissionHandlers + viewModel.globalFlags)

        registerOnBackPressedListener {
            viewModel.backPressHandler(
                onFirstPress = { showToast(resources.getString(R.string.tap_again_to_exit)) },
                onSecondPress = { finishAffinity() }
            )
        }

        setFabColor(getColor(com.w2sv.common.R.color.low_alpha_gray))
    }

    override fun getPages(): List<OnboardingPage> =
        listOf(
            OnboardingPage(
                titleTextRes = R.string.onboarding_page_1_title,
                backgroundColorRes = com.w2sv.common.R.color.magenta_saturated,
                emblemDrawableRes = com.w2sv.common.R.drawable.logo_nobackground,
                descriptionText = getString(R.string.onboarding_page_1_description)
            ),
            OnboardingPage(
                titleTextRes = R.string.screenshot_listening,
                descriptionTextRes = R.string.screenshot_listener_description,
                backgroundColorRes = android.R.color.holo_purple,
                emblemDrawableRes = com.w2sv.common.R.drawable.ic_hearing_24,
                actionLayoutRes = R.layout.action_layout_screenshotlistener_onboardingpage,
                onViewCreatedListener = { view, activity ->
                    val doneAnimation = view.findViewById<LottieAnimationView>(R.id.done_animation)
                    val enableButton = view.findViewById<AppCompatButton>(R.id.enable_button)

                    val viewModel by activity.viewModels<ViewModel>()

                    when (viewModel.screenshotListeningEnabled) {
                        true -> crossVisualize(enableButton, doneAnimation)
                        false -> enableButton
                            .setOnClickListener {
                                (activity as OnboardingActivity)
                                    .screenshotListeningPermissionHandlers
                                    .requestPermissions(
                                        onGranted = {
                                            ScreenshotListener.startService(activity)
                                            viewModel.screenshotListeningEnabled = true
                                        },
                                        onRequestDismissed = {
                                            it
                                                .animationComposer(Techniques.ZoomOut, 750L)
                                                .onEnd {
                                                    with(doneAnimation) {
                                                        show()
                                                        playAnimation()
                                                    }
                                                }
                                                .play()
                                        }
                                    )
                            }
                    }
                },
                onPageFullyVisibleListener = { view, activity ->
                    if (view != null && !activity.viewModels<ViewModel>().value.screenshotListeningEnabled) {
                        view
                            .findViewById<AppCompatButton>(R.id.enable_button)
                            .animationComposer(Techniques.Tada)
                            .delay(resources.getLong(R.integer.delay_small))
                            .play()
                    }
                }
            ),
            OnboardingPage(
                titleTextRes = R.string.onboarding_page_2_title,
                emblemDrawableRes = R.drawable.ic_check_24,
                descriptionTextRes = R.string.onboarding_page_2_description,
                backgroundColorRes = com.w2sv.common.R.color.ocean_blue
            )
        )

    override fun onOnboardingFinished() {
        viewModel.globalFlags.onboardingDone = true
        startMainActivity(true, Animatoo::animateSwipeLeft)
    }
}