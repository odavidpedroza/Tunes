package com.davidpedroza.tunes.core.network.itunes

import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit definition of the two iTunes Search API endpoints the app uses. */
internal interface ItunesService {

    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("limit") limit: Int,
        @Query("country") country: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
    ): ItunesResponse

    @GET("lookup")
    suspend fun lookup(
        @Query("id") id: Long,
        @Query("country") country: String,
        @Query("entity") entity: String = "song",
    ): ItunesResponse
}

/** Store front used for searches and lookups. */
internal data class ItunesConfig(val country: String)
