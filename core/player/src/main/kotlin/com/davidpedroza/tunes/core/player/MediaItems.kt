package com.davidpedroza.tunes.core.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.davidpedroza.tunes.core.model.Artwork
import com.davidpedroza.tunes.core.model.Song

/**
 * Builds the item the session plays. The metadata is what the system shows on the lock screen,
 * in the notification and on connected devices, so title/artist/artwork all go in.
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
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .build(),
    )
    .build()
