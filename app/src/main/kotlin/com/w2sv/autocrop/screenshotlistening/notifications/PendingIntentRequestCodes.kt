package com.w2sv.autocrop.screenshotlistening.notifications

import slimber.log.i

class PendingIntentRequestCodes(baseSeed: Int) : UniqueAssociatedIds(baseSeed) {

    fun makeAndAdd(): Int =
        getNewId()
            .also {
                add(it)
                i { "Added pendingRequestCode $it" }
            }

    fun makeAndAddMultiple(n: Int): ArrayList<Int> =
        ArrayList((0 until n)
            .map { makeAndAdd() })
}