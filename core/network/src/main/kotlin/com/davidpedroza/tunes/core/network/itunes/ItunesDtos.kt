package com.davidpedroza.tunes.core.network.itunes

import kotlinx.serialization.Serializable

@Serializable
internal data class ItunesResponse(
    val resultCount: Int = 0,
    val results: List<ItunesResultDto> = emptyList(),
)

/**
 * A single search/lookup result. The API returns heterogeneous objects (tracks, collections,
 * artists) in the same array, so every field is optional and [wrapperType]/[kind] decide the mapping.
 */
@Serializable
internal data class ItunesResultDto(
    val wrapperType: String? = null,
    val kind: String? = null,
    val collectionType: String? = null,
    val trackId: Long? = null,
    val collectionId: Long? = null,
    val artistName: String? = null,
    val trackName: String? = null,
    val collectionName: String? = null,
    val artworkUrl100: String? = null,
    val artworkUrl60: String? = null,
    val previewUrl: String? = null,
    val trackTimeMillis: Long? = null,
    val trackNumber: Int? = null,
    val trackCount: Int? = null,
    val primaryGenreName: String? = null,
    val releaseDate: String? = null,
    val copyright: String? = null,
)
