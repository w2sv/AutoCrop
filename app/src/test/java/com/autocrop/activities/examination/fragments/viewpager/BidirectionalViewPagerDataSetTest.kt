package com.autocrop.activities.examination.fragments.viewpager

import com.autocrop.utils.rotatedIndex
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class BidirectionalViewPagerDataSetTest {

    companion object{
        private const val SIZE = 10
    }
    private val dataSet = BidirectionalViewPagerDataSet(
        (0 until SIZE)
            .toMutableList()
    )

    @Test
    fun correctInitialization(){
        Assert.assertEquals(dataSet.lastIndex, dataSet.tailPosition)
    }

    @ParameterizedTest
    @CsvSource(
        "9, 78789",
        "2, 3243242",
        "2, 2",
        "0, 0"
    )
    fun correspondingPosition(expected: Int, viewPosition: Int) {
        Assert.assertEquals(expected, dataSet.correspondingPosition(viewPosition))
    }

    @Test
    fun atCorrespondingPosition(){
        val viewPosition = 2

        Assert.assertEquals(viewPosition, dataSet.atCorrespondingPosition(viewPosition))
    }

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
        dataSet.tailPosition = tailPosition
        Assert.assertEquals(expected, dataSet.pageIndex(position))
    }

    @ParameterizedTest
    @ValueSource(
        ints = [67234, 75, 966, 2457, 9, 53, 1, 58, 4, 56, 34, 999, 364, 0]
    )
    fun removeAtAndRealign(viewPagerPosition: Int) {
        val correspondingPosition = dataSet.correspondingPosition(viewPagerPosition)
        val removingAtTail = dataSet.removingAtTail(correspondingPosition)
        val subsequentTailPosition = if (removingAtTail) dataSet.rotatedIndex(dataSet.tailPosition, -1) else dataSet.tailPosition
        val subsequentTailHash = dataSet[subsequentTailPosition].hashCode()
        val newViewPagerPosition: Int = viewPagerPosition + dataSet.viewPositionIncrement(removingAtTail)
        val newViewPagerPositionCropBundleHash = dataSet.atCorrespondingPosition(newViewPagerPosition).hashCode()

        dataSet.removeAtAndRealign(correspondingPosition, removingAtTail, newViewPagerPosition)

        Assert.assertEquals(SIZE - 1, dataSet.size)
        Assert.assertEquals(newViewPagerPositionCropBundleHash, dataSet.atCorrespondingPosition(newViewPagerPosition).hashCode())
        Assert.assertEquals(subsequentTailHash, dataSet[dataSet.tailPosition].hashCode())
    }
}