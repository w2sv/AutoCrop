package com.w2sv.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.espresso.intent.matcher.IntentMatchers.hasPackage
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.FlakyTest
import androidx.test.runner.permission.PermissionRequester
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.android.IMAGE_MIME_TYPE
import de.mannodermaus.junit5.ActivityScenarioExtension
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import utils.espresso.SLOW_TIMEOUT
import utils.espresso.check
import utils.espresso.intentTester
import utils.espresso.isDisplayed
import utils.espresso.retryFlakyAction

internal class MainActivityTest {
    @JvmField
    @RegisterExtension
    val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>()

    companion object {
        @JvmStatic
        @BeforeAll
        fun grantPermissionsIfRequired() {
            if (Build.VERSION.SDK_INT <= 29)
                PermissionRequester().apply {
                    addPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
                    .requestPermissions()
        }
    }

    @Test
    fun canvasContainerVisible() {
        onView(withId(R.id.canvas_container))
            .check(isCompletelyDisplayed())
    }

    @Nested
    inner class ImageSelectionButton {
        @Test
        fun visibleAndClickable() {
            with(onView(withText(R.string.select_images))) {
                check(isClickable())
                check(isCompletelyDisplayed())
            }
        }

        /**
         * https://stackoverflow.com/a/44696077/12083276
         * https://stackoverflow.com/a/58049238/12083276
         */
        @Test
        @FlakyTest
        fun triggersMultipleImageSelectionIntent() =
            intentTester {
                retryFlakyAction(SLOW_TIMEOUT) {
                    onView(withText(R.string.select_images))
                        .perform(ViewActions.click())
                }

                intended(
                    allOf(
                        hasType(IMAGE_MIME_TYPE),
                        hasAction(Intent.ACTION_PICK),
                        hasExtraWithKey(Intent.EXTRA_ALLOW_MULTIPLE)
                    )
                )
            }
    }

    @Nested
    inner class DrawerTest {
        private val toggleButton = onView(withId(R.id.navigation_drawer_button_arrow))

        @Test
        fun inflationButtonDisplayedAndClickable() {
            with(toggleButton) {
                check(isDisplayed())
                check(isClickable())
            }
        }

        @Nested
        inner class ItemsTest {

            @BeforeEach
            fun openDrawer() {
                if (!onView(withId(R.menu.flowfield)).isDisplayed())
                    retryFlakyAction {
                        toggleButton.perform(ViewActions.click())
                    }
            }

            @Test
            fun itemsVisibleAndClickable() {
                listOf(
                    R.string.menu_item_auto_scroll,
                    R.string.menu_item_about,
                    R.string.menu_item_rate_the_app,
                    R.string.menu_item_change_directory,
                    R.string.code
                )
                    .forEach {
                        with(onView(withId(it))) {
                            check(isDisplayed())
                            // check(isClickable())  // fails for some reason
                        }
                    }
            }

            @Test
            fun autoScroll() {
                val userPreferencesValueBeforeClick = BooleanPreferences.autoScroll

                with(onView(withText(R.string.menu_item_auto_scroll))) {
                    perform(ViewActions.click())
                    check(isDisplayed())  // check menu is being persisted on click
                }

                Assertions.assertEquals(!userPreferencesValueBeforeClick, BooleanPreferences.autoScroll)
            }

            @Test
            fun rateTheApp() =
                intentTester {
                    onView(withId(R.id.main_menu_item_rate_the_app))
                        .perform(ViewActions.click())

                    intended(
                        allOf(
                            hasAction(Intent.ACTION_VIEW),
                            hasData(Uri.parse("https://play.google.com/store/apps/details?id=com.w2sv.autocrop")),
                            hasPackage("com.android.vending")
                        )
                    )
                }

            @Test
            @FlakyTest
            fun changeCropSavingDirectory() =
                intentTester {
                    onView(withId(R.id.main_menu_item_change_crop_saving_dir))
                        .perform(ViewActions.click())

                    intended(
                        hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    )
                }

            @Nested
            inner class CropSharingTest {

                private val cropSavingUris = ArrayList<Uri>(
                    listOf(
                        Uri.Builder().authority("something").build()
                    )
                )

                @JvmField
                @RegisterExtension
                val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>(
                    Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
                        .putParcelableArrayListExtra(
                            "com.w2sv.autocrop.CROP_URIS",
                            cropSavingUris
                        )
                )

                private val button: ViewInteraction
                    get() = onView(withId(R.id.main_menu_item_share_crops))

                @Test
                fun cropShareIntentEmission() =
                    intentTester {
                        button
                            .perform(ViewActions.click())

                        intended(
                            allOf(
                                hasAction(Intent.ACTION_CHOOSER),
                                hasExtra(
                                    equalTo(Intent.EXTRA_INTENT), allOf(
                                        hasExtra(Intent.EXTRA_STREAM, cropSavingUris),
                                        hasType(IMAGE_MIME_TYPE),
                                        hasAction(Intent.ACTION_SEND_MULTIPLE)
                                    )
                                )
                            )
                        )
                    }
            }

            @Nested
            inner class AboutFragmentTest {
                @BeforeEach
                fun invoke() {
                    onView(withId(R.id.main_menu_item_about))
                }

                @Test
                fun fragmentShown() {
                    scenarioExtension.scenario.onActivity {
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                Assertions.assertTrue(it.supportFragmentManager.findFragmentById(R.id.layout) is AboutFragment)
                            },
                            200
                        )
                    }
                }

                @Test
                fun returnToFlowFieldFragmentByBackPress() {
                    scenarioExtension.scenario.onActivity {
                        @Suppress("DEPRECATION")
                        it.onBackPressed()
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                Assertions.assertTrue(it.supportFragmentManager.findFragmentById(R.id.layout) is FlowFieldFragment)
                            },
                            100
                        )
                    }
                }
            }
        }
    }

    //    @Test
    //    fun appExit() {
    //        scenarioExtension.scenario.onActivity {
    //            @Suppress("DEPRECATION")
    //            it.onBackPressed()
    //            Assertions.assertTrue(it.isFinishing)
    //        }
    //    }
}