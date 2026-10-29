package com.davidpedroza.tunes.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArtworkTest {

    @Test
    fun `sized rewrites the size token of a store url`() {
        val url = "https://is1-ssl.mzstatic.com/image/thumb/Music/v4/ab/cd/source/100x100bb.jpg"

        assertEquals(
            "https://is1-ssl.mzstatic.com/image/thumb/Music/v4/ab/cd/source/600x600bb.jpg",
            Artwork.sized(url, 600),
        )
    }

    @Test
    fun `sized leaves urls without a size token untouched`() {
        val url = "https://example.com/cover.png"

        assertEquals(url, Artwork.sized(url, 600))
    }

    @Test
    fun `sized returns null for null input`() {
        assertNull(Artwork.sized(null, 600))
    }
}
