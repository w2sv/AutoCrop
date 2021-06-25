package com.autocrop.activities.main

import android.content.Intent
import android.os.SystemClock
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
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
import utils.UserPreferencesModifyingTest
import utils.espresso.*
import kotlin.test.assertEquals


@RunWith(AndroidJUnit4ClassRunner::class)
@MediumTest
class MainActivityTest: UserPreferencesModifyingTest() {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun layoutVisibility() {
        assertCompleteVisibility(R.id.layout_activity_main)
    }

    @Test
    fun canvasContainerVisibility() {
        assertCompleteVisibility(R.id.canvas_container)
    }

    @Test
    @FlakyTest
    fun flowfieldCaptureButton() {
        // TODO: debug

        with(viewInteractionById(R.id.flowfield_capture_button)) {
            check(isDisplayed())
            check(isClickable())

            activityScenarioRule.scenario.onActivity {
                with(it.flowfieldCapturesDestinationDir){
                    fun nContainedFiles(): Int = listFiles()!!.size

                    val nFilesPreNewCapture = nContainedFiles()
                    retryFlakyAction(200) { click() }
                    SystemClock.sleep(500)
                    assertEquals(nFilesPreNewCapture + 1, nContainedFiles())
                }
            }
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

        assertCompleteVisibility(id)
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
    fun userPreferencesPersistingThroughoutAppDestruction() {
        val expectedUserPreferenceValues = UserPreferences
            .values
            .map { !it }
            .toTypedArray()

        setUserPreferences(expectedUserPreferenceValues)

        activityScenarioRule.scenario.recreate()
        Assert.assertArrayEquals(expectedUserPreferenceValues, UserPreferences.values.toTypedArray())
    }
}