package com.ceylonapz.babytube.presentation.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackHistoryTest {

    @Test
    fun `records the latest video and clears once consumed`() {
        val history = PlaybackHistory()

        history.record("Lullabies", "a")
        history.record("Lullabies", "b")
        assertEquals(LastPlayed("Lullabies", "b"), history.lastPlayed.value)

        history.consume()
        assertNull(history.lastPlayed.value)
    }
}
