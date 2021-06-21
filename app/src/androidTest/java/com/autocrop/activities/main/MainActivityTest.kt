package com.autocrop.activities.main

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.autocrop.UserPreferences
import com.w2sv.autocrop.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

fun ViewInteraction.isPopupMenuItem(): ViewInteraction = inRoot(isPlatformPopup())


/**
 * https://www.youtube.com/watch?v=deXEAAaznVY
 */

@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    private fun assertVisibility(id: Int){
        onView(withId(id))
            .check(matches(isDisplayed()))
    }

    @Test
    fun layoutVisibility(){
        assertVisibility(R.id.layout_activity_main)
    }

    @Test
    fun canvasContainerVisibility(){
        assertVisibility(R.id.canvas_container)
    }

    @Test
            /**
             * https://stackoverflow.com/a/44696077/12083276
             * https://stackoverflow.com/a/58049238/12083276
             */
    fun imageSelectionButton(){
        val id: Int = R.id.image_selection_button

        Intents.init()

        assertVisibility(id)

        // assert setting of correct button text
        onView(withId(id))
            .check(
                matches(
                    withText(
                        R.string.image_selection_button
                    )
                )
            )

        onView(withId(id))
            .perform(click())

        intended(
            allOf(
                hasType("image/*"),
                hasAction(Intent.ACTION_PICK),
                hasExtraWithKey(Intent.EXTRA_ALLOW_MULTIPLE)
            )
        )

        Intents.release()
    }

    @Test
    fun screenshotButton(){
        assertVisibility(R.id.screenshot_button)
    }

    @Test
    fun menuInflationButton(){
        val buttonId: Int = R.id.menu_button
        val menuItemTexts: List<Int> = listOf(
            R.string.menu_item_conduct_auto_scrolling,
            R.string.menu_item_delete_input_screenshots,
            R.string.menu_item_save_to_autocrop_folder,
            R.string.menu_item_examination_group,
            R.string.menu_item_saving_group
        )

        fun assertItemInvisibility(){
            menuItemTexts.forEach {
                onView(
                    withText(it)
                )
                    .check(doesNotExist())
            }
        }

        assertVisibility(buttonId)
        assertItemInvisibility()

        // inflate menu
        val button = onView(withId(buttonId))

        button
            .perform(click())

        // assert item visibility
        menuItemTexts.forEach {
            onView(
                withText(it)
            )
                .isPopupMenuItem()
                .check(matches(isDisplayed()))
        }

        // assert menu perseverance after item clicking
        menuItemTexts.forEach {
            onView(
                withText(it)
            )
                .isPopupMenuItem()
                .perform(click())
                .perform(click())
        }

        // assert menu closing upon clicking next to menu
        button
            .perform(click())
        assertItemInvisibility()

        // assert menu item check state being reflected within UserPreferences
        val testUserPreferencesValue = false

        val userPreferencesCopy: List<Boolean> = UserPreferences.values.toList()
        UserPreferences.keys.forEach {
            UserPreferences[it] = testUserPreferencesValue
        }

        (0 until UserPreferences.size).forEach {
            onView(
                withText(menuItemTexts[it])
            )
                .isPopupMenuItem()
                .perform(click())
        }

        assertArrayEquals(
            Array(UserPreferences.size){testUserPreferencesValue},
            UserPreferences.values.toTypedArray()
        )

        // reset UserPreferences values
        UserPreferences.keys.forEachIndexed { i, el ->
            UserPreferences[el] = userPreferencesCopy[i]
        }
    }

    @Test
    fun appExitingOnBackpress(){
        pressBackUnconditionally()

        activityScenarioRule.scenario.onActivity {
            assertEquals(it.isDestroyed, true)
        }
    }
}