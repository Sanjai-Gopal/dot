package com.dj.dot.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepTimerTest {
    @Test
    fun `SLEEP_AT_END_OF_SONG constant is minus one`() {
        assertEquals(-1L, SLEEP_AT_END_OF_SONG)
    }

    @Test
    fun `sleep timer sentinel is distinguishable from active countdown timestamps`() {
        val off = 0L
        val endOfSong = SLEEP_AT_END_OF_SONG
        val activeTimestamp = 500_000L

        assertEquals(0L, off)
        assertEquals(-1L, endOfSong)
        assertTrue(activeTimestamp > 0L)
        assertTrue(endOfSong < 0L)
    }
}
