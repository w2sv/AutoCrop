package com.autocrop.activities.main

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.FlakyTest
import com.autocrop.UserPreferences
import com.w2sv.autocrop.R
import org.junit.Assert
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import utils.UserPreferencesModifyingTest
import utils.espresso.*


class MenuTest: UserPreferencesModifyingTest() {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    private val buttonView = viewInteractionById(R.id.menu_button)

    @Test
    fun inflationButtonDisplayedAndClickable() {
        with(buttonView){
            check(isDisplayed())
            check(isClickable())
        }
    }

    @Nested
    @DisplayName("Items Test")
    inner class ItemsTest{

        @BeforeEach
        fun inflateMenu(){
            retryFlakyAction(1000) {
                buttonView.perform(click())
            }
        }

        private val menuTextIds: List<Int>
            get() = menuItemTextIds + menuDividerTextIds

        private val menuItemTextIds: List<Int> = listOf(
            R.string.menu_item_conduct_auto_scrolling,
            R.string.menu_item_delete_input_screenshots,
            R.string.menu_item_save_to_autocrop_folder
        )
        private val menuDividerTextIds: List<Int> = listOf(
            R.string.menu_item_examination_group,
            R.string.menu_item_saving_group
        )

        @Test
        @FlakyTest
        fun itemVisibilityAndClickability() {
            with(menuTextIds) {
                forEach {
                    with(popupMenuItemByTextId(it)){
                        check(isDisplayed())
                        check(isClickable())
                    }
                }
            }

            with(menuDividerTextIds){
                forEach {
                    with(popupMenuItemByTextId(it)){
                        check(isDisplayed())
                    }
                }
            }
        }

        @Test
        @FlakyTest
        fun menuPersistingAfterItemClicking() {
            // click on arbitrary item
            popupMenuItemByTextId(menuItemTextIds[0])
                .perform(click())

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
        fun assertCheckReflectionWithinUserPreferences() {
            // set all UserPreferences to false
            setUserPreferences(Array(UserPreferences.size){false})

            // inflate menu, toggle all item values
//            inflateMenu()
            menuItemTextIds.forEach {
                popupMenuItemByTextId(it)
                    .perform(click())
            }

            // check for equality
            Assert.assertArrayEquals(
                Array(UserPreferences.size) { true },
                UserPreferences.values.toTypedArray()
            )
        }
    }
}