package com.davidpedroza.tunes.core.network.itunes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItunesPaginationTest {

    @Test
    fun `limit grows with the page and is capped at 200`() {
        assertEquals(25, ItunesPagination.limitFor(page = 0, pageSize = 25))
        assertEquals(50, ItunesPagination.limitFor(page = 1, pageSize = 25))
        assertEquals(200, ItunesPagination.limitFor(page = 7, pageSize = 25))
        assertEquals(200, ItunesPagination.limitFor(page = 50, pageSize = 25))
    }

    @Test
    fun `pageOf drops the items delivered by previous pages`() {
        val results = (1..50).toList()

        val page = ItunesPagination.pageOf(results, page = 1, pageSize = 25)

        assertEquals((26..50).toList(), page.items)
        assertFalse(page.endReached)
    }

    @Test
    fun `pageOf reports the end when the api returns fewer items than requested`() {
        val results = (1..40).toList()

        val page = ItunesPagination.pageOf(results, page = 1, pageSize = 25)

        assertEquals((26..40).toList(), page.items)
        assertTrue(page.endReached)
    }

    @Test
    fun `pageOf reports the end once the api cap is hit`() {
        val results = (1..200).toList()

        val page = ItunesPagination.pageOf(results, page = 7, pageSize = 25)

        assertEquals((176..200).toList(), page.items)
        assertTrue(page.endReached)
    }

    @Test
    fun `pageOf is empty when a previous page already consumed everything`() {
        val results = (1..10).toList()

        val page = ItunesPagination.pageOf(results, page = 1, pageSize = 25)

        assertTrue(page.items.isEmpty())
        assertTrue(page.endReached)
    }
}
