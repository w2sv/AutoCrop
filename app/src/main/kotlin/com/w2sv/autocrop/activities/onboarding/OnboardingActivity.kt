package com.w2sv.autocrop.activities.onboarding

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import com.airbnb.lottie.LottieAnimationView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.extensions.crossVisualize
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.preferences.Flags
import com.w2sv.autocrop.screenshotlistening.ScreenshotListener
import com.w2sv.autocrop.ui.animationComposer
import com.w2sv.onboarding.OnboardingPage
import com.w2sv.permissionhandler.requestPermissions
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : com.w2sv.onboarding.OnboardingActivity() {

    @Inject
    lateinit var flags: Flags

    @HiltViewModel
    class ViewModel @Inject constructor() : androidx.lifecycle.ViewModel() {

        companion object {
            fun getInstance(activity: Activity): ViewModel =
                (activity as ComponentActivity).viewModels<ViewModel>().value
        }

        var enabledScreenshotListening: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishAffinity()
                }
            }
        )

        lifecycle.addObserver(flags)
        screenshotListeningPermissionHandlers.forEach {
            lifecycle.addObserver(it)
        }

        setPages(
            listOf(
                OnboardingPage(
                    titleTextRes = R.string.onboarding_page_1_title,
                    backgroundColorRes = R.color.magenta_saturated,
                    emblemDrawableRes = R.drawable.logo_nobackground,
                    descriptionText = getString(R.string.onboarding_page_1_description)
                ),
                OnboardingPage(
                    titleTextRes = R.string.screenshot_listening,
                    descriptionTextRes = R.string.screenshot_listener_description,
                    backgroundColorRes = android.R.color.holo_purple,
                    emblemDrawableRes = R.drawable.ic_hearing_24,
                    actionLayoutRes = R.layout.action_layout_screenshotlistener_onboardingpage,
                    onViewCreatedListener = { view, activity ->
                        val enableButton = view.findViewById<AppCompatButton>(R.id.enable_button)
                        val doneAnimation = view.findViewById<LottieAnimationView>(R.id.done_animation)

                        val viewModel = ViewModel.getInstance(activity)

                        if (viewModel.enabledScreenshotListening)
                            crossVisualize(enableButton, doneAnimation)
                        else
                            enableButton.setOnClickListener {
                                (activity as OnboardingActivity).screenshotListeningPermissionHandlers
                                    .requestPermissions(
                                        onGranted = {
                                            ScreenshotListener.startService(activity)
                                            viewModel.enabledScreenshotListening = true
                                        },
                                        onDialogClosed = {
                                            it
                                                .animationComposer(Techniques.ZoomOut, 750L)
                                                .onEnd {
                                                    with(doneAnimation) {
                                                        show()
                                                        playAnimation()
                                                    }
                                                }
                                                .playOn(it)
                                        }
                                    )
                            }
                    },
                    onPageFullyVisibleListener = { view, activity ->
                        if (view != null && !ViewModel.getInstance(activity).enabledScreenshotListening)
                            view.findViewById<AppCompatButton>(R.id.enable_button)
                                .apply {
                                    animationComposer(Techniques.Tada)
                                        .delay(resources.getLong(R.integer.delay_small))
                                        .playOn(this)
                                }
                    }
                ),
                OnboardingPage(
                    titleTextRes = R.string.onboarding_page_2_title,
                    emblemDrawableRes = R.drawable.ic_scissors_24,
                    descriptionTextRes = R.string.onboarding_page_2_description,
                    backgroundColorRes = R.color.ocean_blue
                )
            )
        )

        setFabColor(getThemedColor(R.color.low_alpha_gray))
    }

    private val screenshotListeningPermissionHandlers by lazy {
        ScreenshotListener.permissionHandlers(this)
    }

    override fun onOnboardingFinished() {
        flags.onboardingDone = true
        MainActivity.start(this, true, Animatoo::animateSwipeLeft)
    }
}