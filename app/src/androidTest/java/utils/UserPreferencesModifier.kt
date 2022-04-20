package utils

import com.autocrop.global.BooleanUserPreferences
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass


/**
 * Interface for test classes tampering with UserPreferences singleton,
 * taking care of restoring the respective original values on test wrap up
 */
abstract class UserPreferencesModifier {

    private lateinit var userPreferencesOnTestStart: Array<Boolean>

    @BeforeClass
    fun storeUserPreferences(){
        userPreferencesOnTestStart = BooleanUserPreferences.values.toTypedArray()
    }

    protected fun setUserPreferences(to: Array<Boolean>){
        Assert.assertEquals(to.size, BooleanUserPreferences.size)

        BooleanUserPreferences.keys.forEachIndexed { i, el ->
            BooleanUserPreferences[el] = to[i]
        }
    }

    @AfterClass
    fun restoreOriginalUserPreferences(){
        setUserPreferences(userPreferencesOnTestStart)
    }
}