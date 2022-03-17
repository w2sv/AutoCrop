package utils.espresso

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
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

fun ViewInteraction.check(matcher: Matcher<View>) {
    check(ViewAssertions.matches(matcher))
}

fun ViewInteraction.checkNot(matcher: Matcher<View>) {
    check(ViewAssertions.matches(not(matcher)))
}