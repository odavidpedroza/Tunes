package com.davidpedroza.tunes.core.testing

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData

/** Load states of a fully loaded, idle paging source. */
val idleLoadStates = LoadStates(
    refresh = LoadState.NotLoading(endOfPaginationReached = true),
    prepend = LoadState.NotLoading(endOfPaginationReached = true),
    append = LoadState.NotLoading(endOfPaginationReached = true),
)

/** A static, already loaded [PagingData]; unlike `PagingData.from(list)` it reports idle load states. */
fun <T : Any> pagingDataOf(items: List<T>): PagingData<T> = PagingData.from(items, sourceLoadStates = idleLoadStates)

fun <T : Any> pagingDataOf(vararg items: T): PagingData<T> = pagingDataOf(items.toList())

/** A [PagingData] whose refresh failed with [error] while [items] (possibly empty) are cached. */
fun <T : Any> failedPagingDataOf(error: Throwable, items: List<T> = emptyList()): PagingData<T> = PagingData.from(
    items,
    sourceLoadStates = LoadStates(
        refresh = LoadState.Error(error),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    ),
)
