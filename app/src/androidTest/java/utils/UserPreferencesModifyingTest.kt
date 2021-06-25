package utils

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.autocrop.UserPreferences
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.assertEquals


@RunWith(AndroidJUnit4ClassRunner::class)
abstract class UserPreferencesModifyingTest {

    lateinit var userPreferencesOnTestStart: Array<Boolean>

    @Before
    fun storeUserPreferences(){
        userPreferencesOnTestStart = UserPreferences.values.toTypedArray()
    }

    @After
    fun restoreOriginalUserPreferences(){
        setUserPreferences(userPreferencesOnTestStart)
    }

    protected fun setUserPreferences(to: Array<Boolean>){
        assertEquals(to.size, UserPreferences.size)

        UserPreferences.keys.forEachIndexed { i, el ->
            UserPreferences[el] = to[i]
        }
    }
}