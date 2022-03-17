package utils

import com.autocrop.UserPreferences
import org.junit.After
import org.junit.Assert
import org.junit.Before


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
        Assert.assertEquals(to.size, UserPreferences.size)

        UserPreferences.keys.forEachIndexed { i, el ->
            UserPreferences[el] = to[i]
        }
    }
}