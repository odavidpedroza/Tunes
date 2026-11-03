package com.davidpedroza.tunes.core.player

import android.net.Uri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.davidpedroza.tunes.core.model.Artwork
import com.davidpedroza.tunes.core.model.Song

/**
 * Builds the item the session plays. The metadata is what the system shows on the lock screen,
 * in the notification and on connected devices, so title/artist/artwork all go in. The remaining
 * [Song] fields travel in the metadata extras so the queue can be rebuilt from the session alone
 * (see [toSongOrNull]) when the app process is recreated while the service keeps playing.
 */
internal fun Song.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id.toString())
    .setUri(previewUrl)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(albumTitle)
            .setArtworkUri(Artwork.sized(artworkUrl, Artwork.LARGE)?.let(Uri::parse))
            .setTrackNumber(trackNumber)
            .setGenre(genre)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(
                bundleOf(
                    EXTRA_ALBUM_ID to albumId,
                    EXTRA_PREVIEW_URL to previewUrl,
                    EXTRA_DURATION_MILLIS to durationMillis,
                ),
            )
            .build(),
    )
    .build()

/** Inverse of [toMediaItem]; `null` for items this app did not create. */
internal fun MediaItem.toSongOrNull(): Song? {
    val id = mediaId.toLongOrNull() ?: return null
    val metadata = mediaMetadata
    val extras = metadata.extras ?: return null
    return Song(
        id = id,
        title = metadata.title?.toString().orEmpty(),
        artist = metadata.artist?.toString().orEmpty(),
        albumId = extras.getLong(EXTRA_ALBUM_ID, -1L).takeIf { it >= 0 },
        albumTitle = metadata.albumTitle?.toString(),
        artworkUrl = metadata.artworkUri?.toString(),
        previewUrl = extras.getString(EXTRA_PREVIEW_URL),
        durationMillis = extras.getLong(EXTRA_DURATION_MILLIS, -1L).takeIf { it >= 0 },
        trackNumber = metadata.trackNumber,
        genre = metadata.genre?.toString(),
    )
}

private const val EXTRA_ALBUM_ID = "tunes.albumId"
private const val EXTRA_PREVIEW_URL = "tunes.previewUrl"
private const val EXTRA_DURATION_MILLIS = "tunes.durationMillis"
