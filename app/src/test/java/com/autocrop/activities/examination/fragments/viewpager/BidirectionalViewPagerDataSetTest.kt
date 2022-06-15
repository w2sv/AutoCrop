package com.autocrop.activities.examination.fragments.viewpager

import com.autocrop.utils.rotatedIndex
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import utils.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
internal class BidirectionalViewPagerDataSetTest {

    companion object{
        private fun dataSet(size: Int = 10) =
            BidirectionalViewPagerDataSet(
                (0 until size)
                    .toMutableList()
            )
    }

    @Test
    fun correctInitialization(){
        val dataSet = dataSet()
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
        val dataSet = dataSet()
        Assert.assertEquals(expected, dataSet.correspondingPosition(viewPosition))
    }

    @ParameterizedTest
    @CsvSource(
        "2",
        "0"
    )
    fun atCorrespondingPosition(viewPosition: Int){
        val dataSet = dataSet()

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
        val dataSet = dataSet()
        dataSet.tailPosition = tailPosition
        Assert.assertEquals(expected, dataSet.pageIndex(position))
    }

    @ParameterizedTest
    @CsvSource(
        "4, 8762, 5, 8763",
        "4, 8762, 4, 8761",
        "4, 8762, 3, 8763",
        )
    fun subsequentViewPosition(removePosition: Int, viewPosition: Int, tailPosition: Int, expected: Int){
        val dataSet = dataSet().apply { this.tailPosition = tailPosition }
        Assert.assertEquals(expected, dataSet.subsequentViewPosition(viewPosition, removePosition))
    }

    @ParameterizedTest
    @CsvSource(
        "13, 13, 7",
        "13, 1, 7",
        "13, 2, 7",
        "13, 3, 7",

        "13, 13, 0",
        "13, 26, 0",
        "26, 26, 0",
        "6, 6, 0",
        "3, 3, 0",

        "13, 13, 12",
        "6, 6, 5",
        "3, 3, 2",

        "13, 12, 0",
        "13, 2, 1",
        "13, 2, 2",
        "13, 2, 6",
        "13, 2, 12",

        "2, 5, 0",
        "3, 89, 0",
        "3, 1, 2",
        "3, 2, 2",

        "5, 786234, 2",
        "34, 2132, 33",
        "34, 2132, 0",
        "17, 7, 13",
    )
    fun removeAndRealign(size: Int, viewPosition: Int, tailPosition: Int) {
        val dataSet = dataSet(size)
            .apply{this.tailPosition = tailPosition}

        val correspondingPosition = dataSet.correspondingPosition(viewPosition)
        val subsequentTailHash = dataSet[
                if (dataSet.tailPosition == correspondingPosition)
                    dataSet.rotatedIndex(dataSet.tailPosition, -1)
                else
                    dataSet.tailPosition
        ].hashCode()

        val newViewPagerPosition: Int = dataSet.subsequentViewPosition(viewPosition, correspondingPosition)
        val newViewPagerPositionHash = dataSet.atCorrespondingPosition(newViewPagerPosition).hashCode()

        dataSet.removeAndRealign(correspondingPosition, newViewPagerPosition)

        Assert.assertEquals(size - 1, dataSet.size)
        Assert.assertEquals(newViewPagerPositionHash, dataSet.atCorrespondingPosition(newViewPagerPosition).hashCode())
        Assert.assertEquals(subsequentTailHash, dataSet[dataSet.tailPosition].hashCode())
    }
}