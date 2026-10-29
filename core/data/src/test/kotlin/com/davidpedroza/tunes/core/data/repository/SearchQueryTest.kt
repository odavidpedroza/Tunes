package com.davidpedroza.tunes.core.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchQueryTest {

    @Test
    fun `normalize trims, collapses whitespace and lowercases`() {
        assertEquals("daft punk", SearchQuery.normalize("  Daft   PUNK \n"))
    }

    @Test
    fun `normalize of blank input is empty`() {
        assertEquals("", SearchQuery.normalize("   "))
    }
}
