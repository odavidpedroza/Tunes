package com.davidpedroza.tunes.feature.player

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {

    @Test
    fun `formats minutes and seconds`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:30", formatDuration(30_000))
        assertEquals("1:26", formatDuration(86_000))
        assertEquals("10:05", formatDuration(605_000))
    }

    @Test
    fun `rounds to the nearest second`() {
        assertEquals("0:01", formatDuration(1_499))
        assertEquals("0:02", formatDuration(1_500))
    }

    @Test
    fun `switches to hours past sixty minutes`() {
        assertEquals("1:00:00", formatDuration(3_600_000))
        assertEquals("1:02:03", formatDuration(3_723_000))
    }

    @Test
    fun `negative input is clamped to zero`() {
        assertEquals("0:00", formatDuration(-5_000))
    }
}
