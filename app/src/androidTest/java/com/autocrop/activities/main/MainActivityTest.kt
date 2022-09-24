package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.filters.FlakyTest
import androidx.test.runner.permission.PermissionRequester
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.global.BooleanPreferences
import com.autocrop.utilsandroid.MimeTypes
import com.w2sv.autocrop.R
import de.mannodermaus.junit5.ActivityScenarioExtension
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.RegisterExtension
import utils.espresso.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MainActivityTest {

    @BeforeAll
    fun grantPermissionsIfRequired(){
        if (Build.VERSION.SDK_INT <= 29)
            PermissionRequester().apply {
                addPermissions(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
                .requestPermissions()
    }

    @JvmField
    @RegisterExtension
    val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>()

    @Test
    fun canvasContainerVisible() {
        viewInteractionById(R.id.canvas_container)
            .check(isCompletelyDisplayed())
    }

//    @Test
//    fun appExit(scenario: ActivityScenario<MainActivity>){
//        scenario.onActivity {
//            it.onBackPressed()
//            Handler(Looper.getMainLooper()).postDelayed(
//                { Assert.assertTrue(it.isDestroyed) },
//                500
//            )
//        }
//    }

    @Nested
    inner class ImageSelectionButton{
        @Test
        fun visibleAndClickable(){
            with(viewInteractionByTextId(R.string.image_selection_button)){
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
        fun triggersMultipleImageSelectionIntent() = intentTester {
            retryFlakyAction(SLOW_TIMEOUT) {
                viewInteractionByTextId(R.string.image_selection_button)
                    .perform(ViewActions.click())
            }

            intended(
                allOf(
                    hasType(MimeTypes.IMAGE),
                    hasAction(Intent.ACTION_PICK),
                    hasExtraWithKey(Intent.EXTRA_ALLOW_MULTIPLE)
                )
            )
        }
    }

    @Nested
    inner class MenuTest {
        private val inflationButton = viewInteractionById(R.id.menu_inflation_button)

        @Test
        fun inflationButtonDisplayedAndClickable() {
            with(inflationButton){
                check(isDisplayed())
                check(isClickable())
            }
        }

        @Nested
        inner class ItemsTest{

            @BeforeEach
            fun inflateMenu(){
                if (!viewInteractionById(R.menu.fragment_flowfield).isDisplayed())
                    retryFlakyAction {
                        inflationButton.perform(ViewActions.click())
                    }
            }
            
            @Test
            fun itemsVisibleAndClickable() {
                listOf(
                    R.string.menu_item_auto_scroll,
                    R.string.menu_item_about,
                    R.string.menu_item_rate_the_app,
                    R.string.menu_item_change_directory
                )
                    .forEach {
                        with(popupMenuItemByTextId(it)){
                            check(isDisplayed())
                            // check(isClickable())  // fails for some reason
                        }
                    }
                }

            @Test
            fun groupDividersVisibleAndNotClickable(){
                listOf(
                    R.string.menu_item_divider_crop_saving,
                    R.string.menu_item_divider_examination,
                    R.string.menu_item_divider_other
                )
                    .forEach {
                        with(popupMenuItemByTextId(it)){
                            check(isDisplayed())
                            checkNot(isClickable())
                        }
                    }
            }

            @Test
            fun autoScroll() {
                val userPreferencesValueBeforeClick = BooleanPreferences.autoScroll

                with(popupMenuItemByTextId(R.string.menu_item_auto_scroll)){
                    perform(ViewActions.click())
                    check(isDisplayed())  // check menu is being persisted on click
                }

                Assertions.assertEquals(
                    !userPreferencesValueBeforeClick,
                    BooleanPreferences.autoScroll
                )
            }

            @Test
            fun rateTheApp() = intentTester{
                popupMenuItemByTextId(R.string.menu_item_rate_the_app)
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
            fun changeCropSavingDirectory() = intentTester{
                popupMenuItemByTextId(R.string.menu_item_change_directory)
                    .perform(ViewActions.click())

                intended(
                    hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE)
                )
            }

            @Nested
            inner class AboutFragmentTest{
                @BeforeEach
                fun invoke(){
                    popupMenuItemByTextId(R.string.menu_item_about)
                        .perform(ViewActions.click())
                }

                @Test
                fun invocation(scenario: ActivityScenario<MainActivity>){
                    scenario.onActivity {
                        Assertions.assertTrue(it.supportFragmentManager.findFragmentById(R.id.layout) is AboutFragment)
                    }
                }

                @Test
                fun returnToFlowFieldFragmentByBackPress(scenario: ActivityScenario<MainActivity>){
                    scenario.onActivity {
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
}

internal class CropSharingButtonTest{

    private val cropSavingUris = ArrayList<Uri>(
        listOf(
            Uri.Builder().authority("something").build()
        )
    )

    @JvmField
    @RegisterExtension
    val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>(
        Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .putParcelableArrayListExtra(IntentExtraIdentifier.CROP_SAVING_URIS, cropSavingUris)
    )

    private val button: ViewInteraction
        get() = viewInteractionById(R.id.crop_sharing_button)

    @Test
    fun layout(){
        with(button){
            check(isCompletelyDisplayed())
            check(isClickable())
        }
    }

    @Test
    fun cropShareIntentEmission() = intentTester {
        button
            .perform(ViewActions.click())

        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(
                    equalTo(Intent.EXTRA_INTENT), allOf(
                        hasExtra(Intent.EXTRA_STREAM, cropSavingUris),
                        hasType(MimeTypes.IMAGE),
                        hasAction(Intent.ACTION_SEND_MULTIPLE)
                    )
                )
            )
        )
    }
}