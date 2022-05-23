package com.autocrop.activities.examination.fragments.viewpager

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.autocrop.collections.CropBundle
import org.junit.Assert
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class ViewPagerDataSetTest {

    private val initialSize = 10
    private var viewPagerDataSet = ViewPagerDataSet(
        (0 until initialSize)
            .map { CropBundle(it.toString().toUri(), createBitmap(1, 1, Bitmap.Config.ARGB_8888), it, it) }
            .toMutableList()
    )
    private val initialTailPosition = viewPagerDataSet.lastIndex

    @Test
    fun correctInitialization(){
        Assert.assertEquals(initialSize, viewPagerDataSet.size)
        Assert.assertEquals(initialTailPosition, viewPagerDataSet.tailPosition)
    }

    @ParameterizedTest
    @CsvSource(
        "9, 78789",
        "2, 3243242",
        "2, 2",
        "0, 0"
    )
    fun correspondingPosition(expected: Int, viewPosition: Int) {
        Assert.assertEquals(expected, viewPagerDataSet.correspondingPosition(viewPosition))
    }

    @Test
    fun atCorrespondingPosition(){
        val viewPosition = 2
        Assert.assertEquals(viewPosition.toString().toUri(), viewPagerDataSet.atCorrespondingPosition(viewPosition).screenshotUri)
    }

    @Nested
    inner class TailPositionAlteringMethods{
        @AfterEach
        fun tearDown() = setTailPosition(initialTailPosition)

        @BeforeEach
        fun setup() = setTailPosition(initialTailPosition)

        @ParameterizedTest
        @CsvSource(
            "9, 0, 0",
            "9, 3, 3",
            "9, 9, 9",
            "9, 8, 8",
            "2, 7, 4",
            "2, 9, 6",
            "2, 0, 7",
            "2, 1, 8",
            "2, 2, 9",
            "4, 0, 5",
            "4, 2, 7",
            "4, 7, 2",
            "5, 9, 3"
        )
        fun pageIndex(tailPosition: Int, position: Int, expected: Int) {
            setTailPosition(tailPosition)
            Assert.assertEquals(expected, viewPagerDataSet.pageIndex(position))
        }

        private fun setTailPosition(tailPosition: Int){
            viewPagerDataSet.tailPosition = tailPosition
        }
    }

    @Nested
    inner class DataSetAlteringMethods{
        @ParameterizedTest
        @ValueSource(
            ints = [67234, 75, 966, 2457, 9, 53, 1, 58, 4]
        )
        fun removeAtAndRealign(viewPagerPosition: Int) {
            val dataSetSize = viewPagerDataSet.size
            val correspondingPosition = viewPagerDataSet.correspondingPosition(viewPagerPosition)
            val removingAtTail = viewPagerDataSet.removingAtTail(correspondingPosition)
            val newViewPagerPosition: Int = viewPagerPosition + viewPagerDataSet.viewPositionIncrement(removingAtTail)
            val newViewPagerPositionCropBundleHash = viewPagerDataSet.atCorrespondingPosition(newViewPagerPosition).hashCode()

            viewPagerDataSet.removeAtAndRealign(correspondingPosition, removingAtTail, newViewPagerPosition)

            Assert.assertEquals(dataSetSize - 1, viewPagerDataSet.size)
            Assert.assertEquals(newViewPagerPositionCropBundleHash, viewPagerDataSet.atCorrespondingPosition(newViewPagerPosition).hashCode())
        }
    }
}