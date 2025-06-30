package de.hsb.vibeify.domain.usecase

import de.hsb.vibeify.core.result.Result
import de.hsb.vibeify.domain.model.Song
import de.hsb.vibeify.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case = Eine spezifische Business Logic Operation
 * Klar definiert, testbar, wiederverwendbar
 */
class GetFeaturedSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<Result<List<Song>>> {
        return musicRepository.getFeaturedSongs()
    }
}

class SearchSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(query: String): Result<List<Song>> {
        if (query.isBlank()) {
            return Result.Error("Search query cannot be empty")
        }

        if (query.length < 2) {
            return Result.Error("Search query must be at least 2 characters")
        }

        return musicRepository.searchSongs(query.trim())
    }
}

class LikeSongUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(songId: String): Result<Unit> {
        if (songId.isBlank()) {
            return Result.Error("Invalid song ID")
        }

        return musicRepository.likeSong(songId)
    }
}
