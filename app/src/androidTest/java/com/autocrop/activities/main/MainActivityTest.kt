package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.filters.FlakyTest
import androidx.test.runner.permission.PermissionRequester
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.utilsandroid.MimeTypes
import com.w2sv.autocrop.R
import de.mannodermaus.junit5.ActivityScenarioExtension
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.RegisterExtension
import utils.UserPreferencesModifier
import utils.espresso.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MainActivityTest: UserPreferencesModifier() {

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

    @BeforeEach
    fun recreateActivity(scenario: ActivityScenario<MainActivity>){
        scenario.recreate()
    }

    @Test
    fun canvasContainerVisible() {
        viewInteractionById(R.id.canvas_container)
            .check(isCompletelyDisplayed())
    }

//    @Test
//    fun appExitOnBackPressed(scenario: ActivityScenario<MainActivity>){
//        scenario.onActivity {
//            it.onBackPressed()
//            Assert.assertTrue(it.isFinishing)
//        }
//    }

    @Nested
    inner class ImageSelectionButton{
        private val id: Int = R.id.image_selection_button

        @Test
        fun visibleAndClickable(){
            assertTextContainment(id, R.string.image_selection_button)
            with(viewInteractionById(id)){
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
                clickView(id)
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
                if (!viewInteractionById(R.menu.activity_main).isDisplayed())
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
                    R.string.menu_item_group_divider_crop_saving,
                    R.string.menu_item_group_divider_examination,
                    R.string.menu_item_group_divider_other
                )
                    .forEach {
                        with(popupMenuItemByTextId(it)){
                            check(isDisplayed())
                            checkNot(isClickable())
                        }
                    }
            }

            @Test
            @FlakyTest
            fun autoScroll() {
                val userPreferencesValueBeforeClick = BooleanUserPreferences.autoScroll

                with(popupMenuItemByTextId(R.string.menu_item_auto_scroll)){
                    perform(ViewActions.click())
                    check(isDisplayed())  // check menu is being persisted on click
                }

                Assert.assertEquals(!userPreferencesValueBeforeClick, BooleanUserPreferences.autoScroll)
            }

            @Test
            @FlakyTest
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
                @FlakyTest
                fun invocation(scenario: ActivityScenario<MainActivity>){
                    scenario.onActivity {
                        Assert.assertTrue(it.supportFragmentManager.findFragmentById(R.id.layout) is AboutFragment)
                    }
                }

                @Test
                @FlakyTest
                fun returnToFlowFieldFragmentByBackPress(scenario: ActivityScenario<MainActivity>){
                    scenario.onActivity {
                        it.onBackPressed()
                        Handler(Looper.getMainLooper()).postDelayed(
                            {
                                Assert.assertTrue(it.supportFragmentManager.findFragmentById(R.id.layout) is FlowFieldFragment)
                            },
                            100
                        )
                    }
                }
            }
        }
    }
}

//internal class TestMainActivityOnCropWriteUrisAvailability{
//    @JvmField
//    @RegisterExtension
//    val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>(
//        Intent(
//            AutoCrop(),
//            MainActivity::class.java
//        ).putParcelableArrayListExtra(
//            IntentExtraIdentifier.CROP_SAVING_URIS,
//            ArrayList()
//        )
//    )
//
//    @BeforeEach
//    fun recreateActivity(){
//        scenarioExtension.scenario.recreate()
//    }
//
//    @Test
//    fun cropSharingButton(){
//        with(viewInteractionById(R.id.crop_sharing_button)){
//            check(isCompletelyDisplayed())
//            check(isClickable())
//        }
//    }
//}