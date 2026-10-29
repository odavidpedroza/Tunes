package com.davidpedroza.tunes.core.common

/** Injectable clock so cache freshness rules can be unit-tested deterministically. */
fun interface TimeProvider {
    fun nowMillis(): Long
}
