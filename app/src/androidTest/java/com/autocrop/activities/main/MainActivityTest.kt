package com.autocrop.activities.main

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.w2sv.autocrop.R
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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


//    @get:Rule
//    val intentsTestRule = IntentsTestRule(MainActivity::class.java)

    @Test
            /**
             * https://stackoverflow.com/a/44696077/12083276
             * https://stackoverflow.com/a/58049238/12083276
             */
    fun imageSelectionButton(){
        val id: Int = R.id.image_selection_button

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

//        Intents.init()
//        intended(
//            allOf(
//                hasAction(equalTo(Intent.ACTION_ANSWER)),
//                hasExtraWithKey(Intent.EXTRA_ALLOW_MULTIPLE),
//                hasType("image/*")
//            )
//        )
//        Intents.release()
    }

    @Test
    fun screenshotButton(){
        assertVisibility(R.id.screenshot_button)
    }

    @Test
    fun menuInflationButton(){
        val buttonId: Int = R.id.menu_button
        val menuId: Int = R.menu.activity_main

        assertVisibility(buttonId)
        onView(withId(menuId))
            .check(doesNotExist())

        onView(withId(buttonId))
            .perform(click())

        onView(
            withText(R.string.menu_item_conduct_auto_scrolling)
        )
            .inRoot(
                isPlatformPopup()
            )
            .check(matches(isDisplayed()))
    }
}