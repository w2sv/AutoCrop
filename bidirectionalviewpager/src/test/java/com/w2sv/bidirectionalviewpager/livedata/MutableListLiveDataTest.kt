package com.w2sv.bidirectionalviewpager.livedata

import com.w2sv.utils.InstantExecutorExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
internal class MutableListLiveDataTest {
    class Implementation(data: MutableList<Int>) : MutableListLiveData<Int>(data)

    private val implementation = Implementation(mutableListOf(1, 2, 3))

    @Test
    fun removeAt() {
        var nObserverCalls = 0

        with(implementation) {
            observeForever {
                nObserverCalls += 1
            }

            removeAt(0)
            removeAt(0)
        }

        Assertions.assertEquals(
            3,
            nObserverCalls
        )  // observer called once more than n_manual_post calls due to instantiation
    }
}