package utils.espresso

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not

fun ViewInteraction.check(matcher: Matcher<View>) {
    check(ViewAssertions.matches(matcher))
}

fun ViewInteraction.checkNot(matcher: Matcher<View>) {
    check(ViewAssertions.matches(not(matcher)))
}

fun ViewInteraction.isDisplayed(): Boolean =
    try {
        check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        true
    }
    catch (e: NoMatchingViewException) {
        false
    }
