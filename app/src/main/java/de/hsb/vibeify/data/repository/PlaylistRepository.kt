package de.hsb.vibeify.data.repository

import de.hsb.vibeify.data.model.Playlist


interface PlaylistRepository {
    suspend fun getPlaylistById(id: String): Playlist?
}

