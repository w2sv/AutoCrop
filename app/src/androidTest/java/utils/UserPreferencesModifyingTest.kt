package utils

import com.autocrop.UserPreferences
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass


/**
 * Interface for test classes tampering with UserPreferences singleton,
 * taking care of restoring the respective original values on test wrap up
 */
abstract class UserPreferencesModifyingTest {

    private lateinit var userPreferencesOnTestStart: Array<Boolean>

    @BeforeClass
    fun storeUserPreferences(){
        userPreferencesOnTestStart = UserPreferences.values.toTypedArray()
    }

    protected fun setUserPreferences(to: Array<Boolean>){
        Assert.assertEquals(to.size, UserPreferences.size)

        UserPreferences.keys.forEachIndexed { i, el ->
            UserPreferences[el] = to[i]
        }
    }

    @AfterClass
    fun restoreOriginalUserPreferences(){
        setUserPreferences(userPreferencesOnTestStart)
    }
}