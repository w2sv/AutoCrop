package com.w2sv.bidirectionalviewpager

import com.w2sv.bidirectionalviewpager.utils.extensions.rotatedIndex
import com.w2sv.utils.InstantExecutorExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@ExtendWith(InstantExecutorExtension::class)
internal class BidirectionalViewPagerDataSetTest {

    companion object {
        private fun getDataSet(size: Int = 10, tailPosition: Int? = null) =
            BidirectionalViewPagerDataSet(
                (0 until size)
                    .toMutableList()
            )
                .apply {
                    if (tailPosition != null)
                        this.tailPosition = tailPosition
                }
    }

    @Test
    fun correctInitialization() {
        val dataSet = getDataSet()
        Assertions.assertEquals(dataSet.lastIndex, dataSet.tailPosition)
    }

    @ParameterizedTest
    @CsvSource(
        "9, 78789",
        "2, 3243242",
        "2, 2",
        "0, 0"
    )
    fun correspondingPosition(expected: Int, viewPosition: Int) {
        val dataSet = getDataSet()
        Assertions.assertEquals(expected, dataSet.correspondingPosition(viewPosition))
    }

    @ParameterizedTest
    @CsvSource(
        "2",
        "0"
    )
    fun atCorrespondingPosition(viewPosition: Int) {
        val dataSet = getDataSet()
        Assertions.assertEquals(viewPosition, dataSet.atCorrespondingPosition(viewPosition))
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
        val dataSet = getDataSet(tailPosition = tailPosition)
        Assertions.assertEquals(expected, dataSet.pageIndex(position))
    }

    @ParameterizedTest
    @CsvSource(
        "4, 5, 1",
        "4, 3, 1",
        "4, 4, -1",
    )
    fun subsequentViewPosition(
        tailPosition: Int,
        removePosition: Int,
        expected: Int
    ) {
        val dataSet = getDataSet(tailPosition = tailPosition)
        Assertions.assertEquals(
            expected,
            dataSet.viewPositionIncrement(removePosition)
        )
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
        val dataSet = getDataSet(size, tailPosition = tailPosition)

        val correspondingPosition = dataSet.correspondingPosition(viewPosition)
        val subsequentTailHash = dataSet[
                if (dataSet.tailPosition == correspondingPosition)
                    dataSet.rotatedIndex(dataSet.tailPosition, -1)
                else
                    dataSet.tailPosition
        ].hashCode()

        val newViewPagerPosition: Int =
            viewPosition + dataSet.viewPositionIncrement(correspondingPosition)
        val newViewPagerPositionHash =
            dataSet.atCorrespondingPosition(newViewPagerPosition).hashCode()

        dataSet.removeAndRealign(correspondingPosition, newViewPagerPosition)

        Assertions.assertEquals(size - 1, dataSet.size)
        Assertions.assertEquals(
            newViewPagerPositionHash,
            dataSet.atCorrespondingPosition(newViewPagerPosition).hashCode()
        )
        Assertions.assertEquals(subsequentTailHash, dataSet[dataSet.tailPosition].hashCode())
    }
}