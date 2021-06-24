package utils.espresso

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher
import utils.espresso.viewInteractionById

fun assertTextContainment(viewId: Int, text: Int) {
    viewInteractionById(viewId)
        .check(
            ViewAssertions.matches(
                ViewMatchers.withText(
                    text
                )
            )
        )
}

fun assertVisibility(viewId: Int) {
    Espresso.onView(ViewMatchers.withId(viewId))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
}

fun ViewInteraction.check(matcher: Matcher<View>) {
    check(ViewAssertions.matches(matcher))
}