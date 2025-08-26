package com.w2sv.common.util

import slimber.log.i

inline fun <T> T.log(makeLogMessage: (T) -> String = { it.toString() }): T =
    also { i { makeLogMessage(it) } }
