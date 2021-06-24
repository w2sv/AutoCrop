package com.autocrop.activities.main

import android.content.Intent
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.FlakyTest
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.autocrop.UserPreferences
import com.w2sv.autocrop.R
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import utils.espresso.*


@RunWith(AndroidJUnit4ClassRunner::class)
@MediumTest
class MainActivityTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun layoutVisibility() {
        assertVisibility(R.id.layout_activity_main)
    }

    @Test
    fun canvasContainerVisibility() {
        assertVisibility(R.id.canvas_container)
    }

    @Test
    @FlakyTest
    fun screenshotButton() {
        with(viewInteractionById(R.id.screenshot_button)) {
            check(isDisplayed())
            check(isClickable())
            retryFlakyAction(200) { click() }
        }
    }

    /**
     * https://stackoverflow.com/a/44696077/12083276
     * https://stackoverflow.com/a/58049238/12083276
     */
    @Test
    @FlakyTest
    fun imageSelectionButton() = intentTester {
        val id: Int = R.id.image_selection_button

        assertVisibility(id)
        assertTextContainment(id, R.string.image_selection_button)

        // assert opening of gallery with multiple image selection intent
        retryFlakyAction(700) {
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

    @Test
    fun userPreferencesMaintenanceThroughoutAppDestruction() {
        // TODO: assert test validity

        val targetValues = arrayOf(true, true, false)

        UserPreferences.keys.forEachIndexed { i, el ->
            UserPreferences[el] = targetValues[i]
        }

        activityScenarioRule.scenario.recreate()

        Assert.assertArrayEquals(targetValues, UserPreferences.values.toTypedArray())
    }

    @Test
    fun intent(){
        activityScenarioRule.scenario.onActivity {
        }
    }
}