package de.hsb.vibeify.services

import de.hsb.vibeify.data.repository.UserRepository
import de.hsb.vibeify.data.repository.UserStatusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowService @Inject constructor(
    private val userRepository: UserRepository,
    private val userStatusRepository: UserStatusRepository,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun followerList(userId: String) = userStatusRepository.getFollowersFlow(userId)
    fun followingList(userId: String) = userStatusRepository.getFollowingFlow(userId)

    fun isFollowingFlow(userId: String, targetUserId: String): Flow<Boolean> =
        userStatusRepository.getFollowingFlow(userId).map { it.contains(targetUserId) }

    fun isFollowerFlow(userId: String, targetUserId: String): Flow<Boolean> =
        userStatusRepository.getFollowersFlow(targetUserId).map { it.contains(userId) }

    suspend fun followUser(currentUserId: String, targetUserId: String): Boolean {
        return try {
            userStatusRepository.addFollower(targetUserId, currentUserId)
            userStatusRepository.addFollowing(currentUserId, targetUserId)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Boolean {
        return try {
            userStatusRepository.removeFollowing(currentUserId, targetUserId)
            userStatusRepository.removeFollower(targetUserId, currentUserId)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun isFollowing(userId: String, targetUserId: String): Boolean {
        return userStatusRepository.isFollowing(userId, targetUserId)
    }

    suspend fun isFollower(userId: String, targetUserId: String): Boolean {
        val followers = userStatusRepository.getFollowers(targetUserId)
        return userId in followers
    }

    suspend fun getFollowers(userId: String): List<String> {
        return userStatusRepository.getFollowers(userId)
    }

    suspend fun getFollowing(userId: String): List<String> {
        return userStatusRepository.getFollowing(userId)
    }
}