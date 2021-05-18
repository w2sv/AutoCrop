package utils

import com.autocrop.utils.biggerEquals
import com.autocrop.utils.biggerThan
import com.autocrop.utils.smallerEquals
import com.autocrop.utils.smallerThan
import org.junit.Test
import kotlin.test.assertEquals

internal class ComparisonTest {
    @Test
    fun smallerThan(){
        assertEquals(false, 4.smallerThan(4))
        assertEquals(true, 4.smallerThan(5))
    }

    @Test
    fun biggerThan(){
        assertEquals(false, 4.biggerThan(4))
        assertEquals(true, 4.biggerThan(3))
    }

    @Test
    fun smallerEquals(){
        assertEquals(true, 4.smallerEquals(4))
        assertEquals(true, 4.smallerEquals(5))
        assertEquals(false, 4.smallerEquals(3))
    }

    @Test
    fun biggerEquals(){
        assertEquals(true, 4.biggerEquals(4))
        assertEquals(true, 4.biggerEquals(3))
        assertEquals(false, 4.biggerEquals(5))
    }
}