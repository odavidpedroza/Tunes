package com.davidpedroza.tunes.core.data.repository

/** Canonical form of a search term, used both as the API term and as the cache key. */
object SearchQuery {
    fun normalize(raw: String): String = raw.trim().replace(Regex("\\s+"), " ").lowercase()
}
