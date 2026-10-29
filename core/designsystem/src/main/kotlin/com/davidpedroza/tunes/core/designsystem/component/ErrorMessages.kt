package com.davidpedroza.tunes.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.davidpedroza.tunes.core.common.TunesException
import com.davidpedroza.tunes.core.designsystem.R

/** Human-readable copy for a failure coming out of the data layer. */
@Composable
fun Throwable.toUserMessage(): String = when (this) {
    is TunesException.NoConnection -> stringResource(R.string.ds_error_no_connection)
    is TunesException.Server -> stringResource(R.string.ds_error_server, code)
    else -> stringResource(R.string.ds_error_unknown)
}
