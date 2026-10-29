package com.davidpedroza.tunes.core.common

/**
 * Failures the data layer surfaces to the UI. Every data source maps its own transport
 * errors (OkHttp, Retrofit, Room...) to one of these so the UI never depends on a library type.
 */
sealed class TunesException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The device is offline or the server could not be reached. */
    class NoConnection(cause: Throwable? = null) : TunesException("No internet connection", cause)

    /** The server answered with an HTTP error. */
    class Server(val code: Int, cause: Throwable? = null) : TunesException("Server error ($code)", cause)

    /** Anything else (malformed payload, bugs...). */
    class Unknown(cause: Throwable? = null) : TunesException(cause?.message ?: "Unexpected error", cause)
}
