package utils.espresso

import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers

fun viewInteractionById(viewId: Int): ViewInteraction = Espresso.onView(ViewMatchers.withId(viewId))

fun viewInteractionByText(text: Int): ViewInteraction = Espresso.onView(ViewMatchers.withText(text))

fun clickView(viewId: Int): ViewInteraction = Espresso.onView(ViewMatchers.withId(viewId))
    .perform(ViewActions.click())

fun ViewInteraction.popupMenuItem(): ViewInteraction = inRoot(RootMatchers.isPlatformPopup())