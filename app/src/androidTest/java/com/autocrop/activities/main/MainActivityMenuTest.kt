package com.autocrop.activities.main

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.FlakyTest
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import check
import com.autocrop.UserPreferences
import com.w2sv.autocrop.R
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import popupMenuItem
import retryFlakyAction
import viewInteractionById
import viewInteractionByText

@RunWith(AndroidJUnit4ClassRunner::class)
@MediumTest
class MenuTest {

    // TODO: make Nested class within MainActivityTest

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    private val view = viewInteractionById(R.id.menu_button)

    private fun inflateMenu(timeout: Long = 700) {
        retryFlakyAction(timeout) {
            view.perform(click())
        }
    }

    private val menuItemTextIds: List<Int> = listOf(
        R.string.menu_item_conduct_auto_scrolling,
        R.string.menu_item_delete_input_screenshots,
        R.string.menu_item_save_to_autocrop_folder
    )

    private val menuDividerTextIds: List<Int> = listOf(
        R.string.menu_item_examination_group,
        R.string.menu_item_saving_group
    )

    private val menuTextIds: List<Int>
        get() = menuItemTextIds + menuDividerTextIds

    private fun assertItemInvisibility() {
        menuTextIds.forEach {
            viewInteractionByText(it)
                .check(ViewAssertions.doesNotExist())
        }
    }

    @Test
    fun button() {
        assertItemInvisibility()
        view.check(isDisplayed())
        view.check(isClickable())
    }

    @Test
    @FlakyTest
    fun itemVisibilityAndMenuPersistingUponItemClicking() {
        inflateMenu(1000)

        // assert item visibility
        with(menuTextIds) {
            forEach {
                viewInteractionByText(it)
                    .popupMenuItem()
                    .check(isDisplayed())

                viewInteractionByText(it)
                    .perform(click())
            }
        }
    }

    @Test
    @FlakyTest
    fun assertCheckReflectionWithinUserPreferences() {
        setUserPreferencesToFalse()
        inflateMenu()

        menuItemTextIds.forEach {
            viewInteractionByText(it)
                .popupMenuItem()
                .perform(click())
        }

        Assert.assertArrayEquals(
            Array(UserPreferences.size) { !testUserPreferencesSetValue },
            UserPreferences.values.toTypedArray()
        )
        resetUserPreferences()
    }

    private val testUserPreferencesSetValue = false

    private fun setUserPreferencesToFalse() {
        UserPreferences.keys.forEach {
            UserPreferences[it] = testUserPreferencesSetValue
        }

        Assert.assertArrayEquals(
            Array(UserPreferences.size) { testUserPreferencesSetValue },
            UserPreferences.values.toTypedArray()
        )
    }

    private fun resetUserPreferences() {
        UserPreferences.keys.forEachIndexed { i, el ->
            UserPreferences[el] = userPreferencesCopy[i]
        }
    }

    private val userPreferencesCopy: List<Boolean> = UserPreferences.values.toList()
}