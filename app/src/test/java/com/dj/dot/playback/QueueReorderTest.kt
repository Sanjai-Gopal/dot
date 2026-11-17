package com.dj.dot.playback

import com.dj.dot.data.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueReorderTest {
    private fun song(id: Long, title: String = "Song $id") = Song(
        id = id, title = title, artist = "Artist", album = "Album", albumId = 1, duration = 180_000,
        track = id.toInt(), year = 2024, path = "/music/$title.mp3", folder = "/music", dateAdded = 0,
    )

    @Test
    fun unshuffledPreservesOriginalOrder() {
        val orig = listOf(song(1), song(2), song(3), song(4))
        val shuffledQueue = listOf(song(3), song(1), song(4), song(2))
        val result = unshuffled(orig, shuffledQueue)
        assertEquals(listOf(1L, 2L, 3L, 4L), result.map { it.id })
    }

    @Test
    fun unshuffledExcludesRemovedSongs() {
        val orig = listOf(song(1), song(2), song(3), song(4), song(5))
        // Song 5 was removed while on shuffle
        val currentQueue = listOf(song(1), song(3), song(2), song(4))
        val result = unshuffled(orig, currentQueue)

        assertEquals(listOf(1L, 2L, 3L, 4L), result.map { it.id })
        assertFalse(result.any { it.id == 5L })
    }

    @Test
    fun unshuffledAppendsNewTracks() {
        val orig = listOf(song(1), song(2), song(3))
        // Song 4 was added while on shuffle
        val currentQueue = listOf(song(2), song(1), song(3), song(4))
        val result = unshuffled(orig, currentQueue)

        assertEquals(listOf(1L, 2L, 3L, 4L), result.map { it.id })
        assertTrue(result.any { it.id == 4L })
    }

    @Test
    fun unshuffledHandlesDuplicateSongsInOriginal() {
        // Queue has two copies of song 2
        val orig = listOf(song(1), song(2), song(3), song(2), song(4))
        val shuffledQueue = listOf(song(2), song(4), song(1), song(2), song(3))
        val result = unshuffled(orig, shuffledQueue)

        assertEquals(listOf(1L, 2L, 3L, 2L, 4L), result.map { it.id })
    }

    @Test
    fun unshuffledHandlesRemovingOneOfTwoDuplicates() {
        // Queue originally had two copies of song 2: [1, 2, 3, 2, 4]
        val orig = listOf(song(1), song(2), song(3), song(2), song(4))
        // User removed one copy of song 2 while shuffled
        val currentQueue = listOf(song(2), song(4), song(1), song(3))
        val result = unshuffled(orig, currentQueue)

        // Exactly one copy of song 2 retained at its first position; second is not resurrected
        assertEquals(listOf(1L, 2L, 3L, 4L), result.map { it.id })
    }
}
