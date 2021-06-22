import android.os.SystemClock
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Matcher
import java.util.concurrent.TimeoutException


fun viewInteractionById(viewId: Int): ViewInteraction = onView(withId(viewId))

fun viewInteractionByText(text: Int): ViewInteraction = onView(withText(text))

fun clickView(viewId: Int): ViewInteraction = onView(withId(viewId))
    .perform(click())

fun ViewInteraction.popupMenuItem(): ViewInteraction = inRoot(RootMatchers.isPlatformPopup())

fun assertTextContainment(viewId: Int, text: Int) {
    viewInteractionById(viewId)
        .check(
            matches(
                withText(
                    text
                )
            )
        )
}

fun assertVisibility(viewId: Int) {
    onView(withId(viewId))
        .check(matches(isDisplayed()))
}

fun ViewInteraction.check(matcher: Matcher<View>) {
    check(matches(matcher))
}

inline fun intentTester(wrappedFun: () -> Unit) {
    Intents.init()
    wrappedFun()
    Intents.release()
}

inline fun retryFlakyAction(timeout: Long, flakyAction: () -> Unit) {
    val startTime = SystemClock.elapsedRealtime()
    var nTries = 0

    while (SystemClock.elapsedRealtime() - startTime < timeout) {
        try {
            return flakyAction()
        } catch (e: PerformException) {
            nTries += 1
        }
    }

    throw TimeoutException("Timed out after $nTries tries in $timeout ms")
}