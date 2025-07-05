# DefaultApi

All URIs are relative to *http://localhost*

| Method                                                                                                                     | HTTP request                              | Description            |
|----------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|------------------------|
| [**getCoverCoverSongIdGet**](DefaultApi.md#getCoverCoverSongIdGet)                                                         | **GET** cover/{song_id}                   | Get Cover              |
| [**getPlaylistCoverCoverPlaylistPlaylistIdGet**](DefaultApi.md#getPlaylistCoverCoverPlaylistPlaylistIdGet)                 | **GET** cover/playlist/{playlist_id}      | Get Playlist Cover     |
| [**getProfilePicturePictureUserIdGet**](DefaultApi.md#getProfilePicturePictureUserIdGet)                                   | **GET** picture/{user_id}                 | Get Profile Picture    |
| [**rootGet**](DefaultApi.md#rootGet)                                                                                       | **GET**                                   | Root                   |
| [**streamSongStreamSongIdGet**](DefaultApi.md#streamSongStreamSongIdGet)                                                   | **GET** stream/{song_id}                  | Stream Song            |
| [**uploadCoverUploadCoverPlaylistIdPost**](DefaultApi.md#uploadCoverUploadCoverPlaylistIdPost)                             | **POST** upload/cover/{playlist_id}       | Upload Cover           |
| [**uploadProfilePictureUploadProfilePictureUserIdPost**](DefaultApi.md#uploadProfilePictureUploadProfilePictureUserIdPost) | **POST** upload/profile-picture/{user_id} | Upload Profile Picture |

Get Cover

### Example

```kotlin
// Import classes:
//import de.hsb.vibeify.api.generated.*
//import de.hsb.vibeify.api.generated.infrastructure.*
//import de.hsb.vibeify.api.generated.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val songId : kotlin.String = songId_example // kotlin.String | 

val result : kotlin.Any = webService.getCoverCoverSongIdGet(songId)
```

### Parameters

| Name       | Type              | Description | Notes |
|------------|-------------------|-------------|-------|
| **songId** | **kotlin.String** |             |       |

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

Get Playlist Cover

### Example

```kotlin
// Import classes:
//import de.hsb.vibeify.api.generated.*
//import de.hsb.vibeify.api.generated.infrastructure.*
//import de.hsb.vibeify.api.generated.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val playlistId: kotlin.String = playlistId_example // kotlin.String | 

val result: kotlin.Any = webService.getPlaylistCoverCoverPlaylistPlaylistIdGet(playlistId)
```

### Parameters

| Name           | Type              | Description | Notes |
|----------------|-------------------|-------------|-------|
| **playlistId** | **kotlin.String** |             |       |

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

Get Profile Picture

### Example

```kotlin
// Import classes:
//import de.hsb.vibeify.api.generated.*
//import de.hsb.vibeify.api.generated.infrastructure.*
//import de.hsb.vibeify.api.generated.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val userId : kotlin.String = userId_example // kotlin.String | 

val result : kotlin.Any = webService.getProfilePicturePictureUserIdGet(userId)
```

### Parameters

| Name       | Type              | Description | Notes |
|------------|-------------------|-------------|-------|
| **userId** | **kotlin.String** |             |       |

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

Root

### Example

```kotlin
// Import classes:
//import de.hsb.vibeify.api.generated.*
//import de.hsb.vibeify.api.generated.infrastructure.*
//import de.hsb.vibeify.api.generated.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)

val result : kotlin.collections.Map<kotlin.String, kotlin.Any> = webService.rootGet()
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**kotlin.collections.Map&lt;kotlin.String, kotlin.Any&gt;**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

Stream Song

### Example

```kotlin
// Import classes:
//import de.hsb.vibeify.api.generated.*
//import de.hsb.vibeify.api.generated.infrastructure.*
//import de.hsb.vibeify.api.generated.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val songId : kotlin.String = songId_example // kotlin.String | 

val result : kotlin.Any = webService.streamSongStreamSongIdGet(songId)
```

### Parameters

| Name       | Type              | Description | Notes |
|------------|-------------------|-------------|-------|
| **songId** | **kotlin.String** |             |       |

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

Upload Cover

Upload a cover image for a playlist

### Example

```kotlin
// Import classes:
//import de.hsb.vibeify.api.generated.*
//import de.hsb.vibeify.api.generated.infrastructure.*
//import de.hsb.vibeify.api.generated.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val playlistId : kotlin.String = playlistId_example // kotlin.String | 
val file : java.io.File = BINARY_DATA_HERE // java.io.File | 

val result : kotlin.Any = webService.uploadCoverUploadCoverPlaylistIdPost(playlistId, file)
```

### Parameters

| **playlistId** | **kotlin.String**| | |
| Name | Type | Description | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **file** | **java.io.File**| | |

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: multipart/form-data
- **Accept**: application/json

Upload Profile Picture

Upload a profile picture for a user

### Example

```kotlin
// Import classes:
//import de.hsb.vibeify.api.generated.*
//import de.hsb.vibeify.api.generated.infrastructure.*
//import de.hsb.vibeify.api.generated.models.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(DefaultApi::class.java)
val userId : kotlin.String = userId_example // kotlin.String | 
val file : java.io.File = BINARY_DATA_HERE // java.io.File | 

val result : kotlin.Any = webService.uploadProfilePictureUploadProfilePictureUserIdPost(userId, file)
```

### Parameters

| **userId** | **kotlin.String**| | |
| Name | Type | Description | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **file** | **java.io.File**| | |

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: multipart/form-data
- **Accept**: application/json

