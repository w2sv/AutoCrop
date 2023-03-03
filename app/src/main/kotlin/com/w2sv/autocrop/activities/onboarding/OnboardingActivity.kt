package com.w2sv.autocrop.activities.onboarding

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import com.airbnb.lottie.LottieAnimationView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.extensions.crossVisualize
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.ui.views.animationComposer
import com.w2sv.autocrop.utils.extensions.registerOnBackPressedListener
import com.w2sv.onboarding.OnboardingPage
import com.w2sv.permissionhandler.requestPermissions
import com.w2sv.preferences.GlobalFlags
import com.w2sv.screenshotlistening.ScreenshotListener
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : com.w2sv.onboarding.OnboardingActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(val globalFlags: GlobalFlags) : androidx.lifecycle.ViewModel() {
        var enabledScreenshotListening: Boolean = false
    }

    val viewModel by viewModels<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildList {
            addAll(screenshotListeningPermissionHandlers)
            add(viewModel.globalFlags)
        }
            .forEach {
                lifecycle.addObserver(it)
            }

        registerOnBackPressedListener {
            finishAffinity()
        }

        setFabColor(getColor(com.w2sv.common.R.color.low_alpha_gray))
    }

    private val screenshotListeningPermissionHandlers by lazy {
        ScreenshotListener.permissionHandlers(this)
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
                    activity as OnboardingActivity
                    val enableButton = view.findViewById<AppCompatButton>(R.id.enable_button)
                    val doneAnimation = view.findViewById<LottieAnimationView>(R.id.done_animation)

                    if (activity.viewModel.enabledScreenshotListening)
                        crossVisualize(enableButton, doneAnimation)
                    else
                        enableButton.setOnClickListener {
                            activity.screenshotListeningPermissionHandlers
                                .requestPermissions(
                                    onGranted = {
                                        ScreenshotListener.startService(activity)
                                        activity.viewModel.enabledScreenshotListening = true
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
                },
                onPageFullyVisibleListener = { view, activity ->
                    if (view != null && !activity.viewModels<ViewModel>().value.enabledScreenshotListening)
                        view.findViewById<AppCompatButton>(R.id.enable_button)
                            .apply {
                                animationComposer(Techniques.Tada)
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
        MainActivity.start(this, true, Animatoo::animateSwipeLeft)
    }
}