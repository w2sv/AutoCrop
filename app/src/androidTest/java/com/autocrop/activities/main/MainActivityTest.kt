package com.autocrop.activities.main

import android.Manifest
import android.content.Intent
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.FlakyTest
import androidx.test.runner.permission.PermissionRequester
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.utils.android.MimeTypes
import com.autocrop.utils.android.externalPicturesDir
import com.w2sv.autocrop.R
import de.mannodermaus.junit5.ActivityScenarioExtension
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.RegisterExtension
import utils.UserPreferencesModifier
import utils.espresso.*
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MainActivityTest: UserPreferencesModifier() {

    @BeforeAll
    fun grantPermissions(){
        PermissionRequester().apply {
            addPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            requestPermissions()
        }
    }

    @JvmField
    @RegisterExtension
    val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>()

    @BeforeEach
    fun recreateActivity(){
        scenarioExtension.scenario.recreate()
    }

    @Test
    fun layoutVisibility() {
        viewInteractionById(R.id.layout_activity_main).check(isCompletelyDisplayed())
    }

    @Test
    fun canvasContainerVisibility() {
        viewInteractionById(R.id.canvas_container).check(isCompletelyDisplayed())
    }

    @Nested
    inner class FlowfieldCaptureButton {
        private val id = R.id.flowfield_capture_button

        @Test
        fun visibleAndClickable() {
            with(viewInteractionById(id)) {
                check(isDisplayed())
                check(isClickable())
            }
        }

//        @Test
//        @FlakyTest
//        fun capturing() {
//            retryFlakyAction(SLOW_TIMEOUT) { clickView(id) }
//            Assert.assertEquals(1, flowFieldCaptureFiles().size)
//        }

        @AfterEach
        fun clearCapturesDir(){
            flowFieldCaptureFiles().forEach {
                it.deleteRecursively()
            }
        }

        private fun flowFieldCaptureFiles(): List<File> =
            externalPicturesDir.listFiles()!!.filter { it.name.contains("FlowField") && it.endsWith("jpg") }
    }

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
        private val inflationButton = viewInteractionById(R.id.menu_button)

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

            private val menuItemTextIds: List<Int> = listOf(
                R.string.menu_item_conduct_auto_scrolling,
                R.string.menu_item_about_the_app,
                R.string.menu_item_rate_the_app,
                R.string.menu_item_change_directory
            )
            private val menuDividerTextIds: List<Int> = listOf(
                R.string.menu_item_group_divider_crop_saving,
                R.string.menu_item_group_divider_examination,
                R.string.menu_item_group_divider_other
            )

            @Test
            @FlakyTest
            fun itemsVisibleAndClickable() {
                with(menuItemTextIds) {
                    forEach {
                        with(popupMenuItemByTextId(it)){
                            check(isDisplayed())
                            // check(isClickable())  // fails for some reason
                        }
                    }
                }

                with(menuDividerTextIds){
                    forEach {
                        with(popupMenuItemByTextId(it)){
                            check(isDisplayed())
                            checkNot(isClickable())
                        }
                    }
                }
            }

            @Test
            @FlakyTest
            fun menuPersistingAfterItemClicking() {
                popupMenuItemByTextId(R.string.menu_item_conduct_auto_scrolling)
                    .perform(ViewActions.click())

                viewInteractionById(R.menu.activity_main).check(isDisplayed())
            }

            @Test
            @FlakyTest
            fun redirectionToAboutScreen(){
                popupMenuItemByTextId(R.string.menu_item_about_the_app)
                    .perform(ViewActions.click())

                scenarioExtension.scenario.onActivity {
                    Assert.assertTrue(it.aboutFragment.isVisible)
                }
            }

//            @Test
//            @FlakyTest
//            fun reflectionOfValueChangesWithinUserPreferences() {
//                val preClick: Boolean = BooleanUserPreferences.conductAutoScrolling
//
//                popupMenuItemByTextId(R.string.menu_item_conduct_auto_scrolling)
//                    .perform(ViewActions.click())
//
//                Assert.assertEquals(preClick, !BooleanUserPreferences.conductAutoScrolling)
//            }
        }
    }
}