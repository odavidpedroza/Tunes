package com.davidpedroza.tunes.core.data.local

import androidx.room.withTransaction
import com.davidpedroza.tunes.core.database.TunesDatabase
import javax.inject.Inject

/** Runs a block atomically against the local store; abstracted so repositories are unit-testable. */
interface TransactionRunner {
    suspend operator fun <R> invoke(block: suspend () -> R): R
}

internal class RoomTransactionRunner @Inject constructor(
    private val db: TunesDatabase,
) : TransactionRunner {
    override suspend fun <R> invoke(block: suspend () -> R): R = db.withTransaction { block() }
}
