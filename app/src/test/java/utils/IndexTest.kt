package utils

import com.autocrop.utils.rotated
import org.junit.Test
import kotlin.test.assertEquals

internal class IndexTest {
    @Test
    fun rotated() {
        assertEquals(2, 1.rotated(1, 7))
        assertEquals(2, 1.rotated(1, 3))
        assertEquals(0, 1.rotated(-1, 7))
        assertEquals(6, 1.rotated(-2, 7))
        assertEquals(5, 1.rotated(4, 7))
        assertEquals(4, 1.rotated(10, 7))
    }
}