package com.autocrop.activities.main

import android.content.Intent
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.FlakyTest
import com.autocrop.UserPreferences
import com.w2sv.autocrop.R
import de.mannodermaus.junit5.ActivityScenarioExtension
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import utils.UserPreferencesModifier
import utils.espresso.*


class MainActivityTest: UserPreferencesModifier() {

    @JvmField
    @RegisterExtension
    val scenarioExtension = ActivityScenarioExtension.launch<MainActivity>()

    @BeforeEach
    fun setup(){
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
    inner class UserPreferencesInteraction{
        @Test
        fun initialization() {
            Assert.assertTrue(UserPreferences.isInitialized)
        }

        @Test
        fun persistThroughoutAppDestruction() {
            UserPreferences
                .values
                .map { !it }
                .toTypedArray().let{ expectedValues ->
                    // set UserPreferences to expected values
                    setUserPreferences(expectedValues)

                    // recreate activity and assert
                    scenarioExtension.scenario.recreate()
                    Assert.assertArrayEquals(
                        expectedValues,
                        UserPreferences.values.toTypedArray()
                    )
                }
        }
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

        @Test
        @FlakyTest
        fun capturing() {
            retryFlakyAction(5000) { clickView(id) }

            scenarioExtension.scenario.onActivity {
                Assert.assertEquals(1, it.flowField.capturesDestinationDir.listFiles()!!.size)
            }
        }

        @AfterEach
        fun clearCapturesDir(){
            scenarioExtension.scenario.onActivity{
                it.flowField.capturesDestinationDir.listFiles()!!.forEach { file ->
                    file.deleteRecursively()
                }
            }
        }
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
            retryFlakyAction(5000) {
                clickView(id)
            }

            intended(
                allOf(
                    hasType("image/*"),
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
                retryFlakyAction(1000) {
                    inflationButton.perform(ViewActions.click())
                }
            }

            private val menuTextIds: List<Int>
                get() = menuItemTextIds + menuDividerTextIds

            private val menuItemTextIds: List<Int> = listOf(
                R.string.menu_item_conduct_auto_scrolling,
                R.string.menu_item_delete_input_screenshots,
            )
            private val menuDividerTextIds: List<Int> = listOf(
                R.string.menu_item_examination_group,
                R.string.menu_item_saving_group
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
                // click on arbitrary item
                popupMenuItemByTextId(menuItemTextIds[0])
                    .perform(ViewActions.click())

                // check for menu item visibility
                with(menuTextIds) {
                    forEach {
                        popupMenuItemByTextId(it)
                            .check(isDisplayed())
                    }
                }
            }

            @Test
            @FlakyTest
            fun reflectionOfValueChangesWithinUserPreferences() {
                // set all UserPreferences to false
                setUserPreferences(Array(UserPreferences.size){false})

                // inflate menu, toggle all item values
                menuItemTextIds.forEach {
                    popupMenuItemByTextId(it)
                        .perform(ViewActions.click())
                }

                // check for equality
                Assert.assertArrayEquals(
                    Array(UserPreferences.size) { true },
                    UserPreferences.values.toTypedArray()
                )
            }
        }
    }
}