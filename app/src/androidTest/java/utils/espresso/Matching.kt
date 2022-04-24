package utils.espresso

import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers

fun viewInteractionById(viewId: Int): ViewInteraction = Espresso.onView(ViewMatchers.withId(viewId))
fun viewInteractionByTextId(textId: Int): ViewInteraction = Espresso.onView(ViewMatchers.withText(textId))

fun clickView(viewId: Int): ViewInteraction =
    viewInteractionById(viewId).perform(ViewActions.click())

fun popupMenuItemByTextId(textId: Int): ViewInteraction =
    viewInteractionByTextId(textId).inRoot(RootMatchers.isPlatformPopup())
