package com.davidpedroza.tunes.core.network.itunes

/**
 * Emulates offset pagination on top of the iTunes Search API.
 *
 * The API documents only `limit` (1..200) and silently ignores `offset`, so page *n* is fetched by
 * requesting `(n + 1) * pageSize` results and dropping the ones already delivered by pages `0..n-1`.
 * The 200-result cap therefore also caps how deep a search can go. Because result order is not
 * guaranteed to be stable between calls, consumers should de-duplicate by song id.
 */
internal object ItunesPagination {
    const val MAX_LIMIT = 200

    data class Page<T>(val items: List<T>, val endReached: Boolean)

    /** `limit` to send for [page]. */
    fun limitFor(page: Int, pageSize: Int): Int = ((page + 1) * pageSize).coerceAtMost(MAX_LIMIT)

    /** Extracts [page] from the full [results] the API returned for [limitFor]. */
    fun <T> pageOf(results: List<T>, page: Int, pageSize: Int): Page<T> {
        val requested = limitFor(page, pageSize)
        val start = (page * pageSize).coerceAtMost(results.size)
        val items = results.subList(start, results.size)
        val endReached = results.size < requested || requested >= MAX_LIMIT
        return Page(items, endReached)
    }
}
