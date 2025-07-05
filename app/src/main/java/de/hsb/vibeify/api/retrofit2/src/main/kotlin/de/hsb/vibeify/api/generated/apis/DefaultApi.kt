package de.hsb.vibeify.api.generated.apis

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface DefaultApi {
    /**
     * GET cover/{song_id}
     * Get Cover
     *
     * Responses:
     *  - 200: Successful Response
     *  - 422: Validation Error
     *
     * @param songId
     * @return [Call]<[kotlin.Any]>
     */
    @GET("cover/{song_id}")
    fun getCoverCoverSongIdGet(@Path("song_id") songId: kotlin.String): Call<kotlin.Any>

    /**
     * GET cover/playlist/{playlist_id}
     * Get Playlist Cover
     *
     * Responses:
     *  - 200: Successful Response
     *  - 422: Validation Error
     *
     * @param playlistId
     * @return [Call]<[kotlin.Any]>
     */
    @GET("cover/playlist/{playlist_id}")
    fun getPlaylistCoverCoverPlaylistPlaylistIdGet(@Path("playlist_id") playlistId: kotlin.String): Call<kotlin.Any>

    /**
     * GET picture/{user_id}
     * Get Profile Picture
     *
     * Responses:
     *  - 200: Successful Response
     *  - 422: Validation Error
     *
     * @param userId
     * @return [Call]<[kotlin.Any]>
     */
    @GET("picture/{user_id}")
    fun getProfilePicturePictureUserIdGet(@Path("user_id") userId: kotlin.String): Call<kotlin.Any>

    /**
     * GET
     * Root
     *
     * Responses:
     *  - 200: Successful Response
     *
     * @return [Call]<[kotlin.collections.Map<kotlin.String, kotlin.Any>]>
     */
    @GET("")
    fun rootGet(): Call<kotlin.collections.Map<kotlin.String, kotlin.Any>>

    /**
     * GET stream/{song_id}
     * Stream Song
     *
     * Responses:
     *  - 200: Successful Response
     *  - 422: Validation Error
     *
     * @param songId
     * @return [Call]<[kotlin.Any]>
     */
    @GET("stream/{song_id}")
    fun streamSongStreamSongIdGet(@Path("song_id") songId: kotlin.String): Call<kotlin.Any>

    /**
     * POST upload/cover/{playlist_id}
     * Upload Cover
     * Upload a cover image for a playlist
     * Responses:
     *  - 200: Successful Response
     *  - 422: Validation Error
     *
     * @param playlistId
     * @param file
     * @return [Call]<[kotlin.Any]>
     */
    @Multipart
    @POST("upload/cover/{playlist_id}")
    fun uploadCoverUploadCoverPlaylistIdPost(
        @Path("playlist_id") playlistId: kotlin.String,
        @Part file: MultipartBody.Part
    ): Call<kotlin.Any>

    /**
     * POST upload/profile-picture/{user_id}
     * Upload Profile Picture
     * Upload a profile picture for a user
     * Responses:
     *  - 200: Successful Response
     *  - 422: Validation Error
     *
     * @param userId
     * @param file
     * @return [Call]<[kotlin.Any]>
     */
    @Multipart
    @POST("upload/profile-picture/{user_id}")
    fun uploadProfilePictureUploadProfilePictureUserIdPost(
        @Path("user_id") userId: kotlin.String,
        @Part file: MultipartBody.Part
    ): Call<kotlin.Any>

}
