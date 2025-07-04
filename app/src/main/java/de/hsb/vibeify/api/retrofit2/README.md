# de.hsb.vibeify.api.generated - Kotlin client library for FastAPI

No description provided (generated by Openapi
Generator https://github.com/openapitools/openapi-generator)

## Overview

This API client was generated by the [OpenAPI Generator](https://openapi-generator.tech) project. By
using the [openapi-spec](https://github.com/OAI/OpenAPI-Specification) from a remote server, you can
easily generate an API client.

- API version: 0.1.0
- Package version:
- Generator version: 7.14.0
- Build package: org.openapitools.codegen.languages.KotlinClientCodegen

## Requires

* Kotlin 1.7.21
* Gradle 7.5

## Build

First, create the gradle wrapper script:

```
gradle wrapper
```

Then, run:

```
./gradlew check assemble
```

This runs all tests and packages the library.

## Features/Implementation Notes

* Supports JSON inputs/outputs, File inputs, and Form inputs.
* Supports collection formats for query parameters: csv, tsv, ssv, pipes.
* Some Kotlin and Java types are fully qualified to avoid conflicts with types defined in OpenAPI
  definitions.
* Implementation of ApiClient is intended to reduce method counts, specifically to benefit Android
  targets.

<a id="documentation-for-api-endpoints"></a>

## Documentation for API Endpoints

All URIs are relative to *http://localhost*

| Class        | Method                                                                                                                          | HTTP request                              | Description            |
|--------------|---------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|------------------------|
| *DefaultApi* | [**getCoverCoverSongIdGet**](docs/DefaultApi.md#getcovercoversongidget)                                                         | **GET** cover/{song_id}                   | Get Cover              |
| *DefaultApi* | [**getPlaylistCoverCoverPlaylistPlaylistIdGet**](docs/DefaultApi.md#getplaylistcovercoverplaylistplaylistidget)                 | **GET** cover/playlist/{playlist_id}      | Get Playlist Cover     |
| *DefaultApi* | [**getProfilePicturePictureUserIdGet**](docs/DefaultApi.md#getprofilepicturepictureuseridget)                                   | **GET** picture/{user_id}                 | Get Profile Picture    |
| *DefaultApi* | [**rootGet**](docs/DefaultApi.md#rootget)                                                                                       | **GET**                                   | Root                   |
| *DefaultApi* | [**streamSongStreamSongIdGet**](docs/DefaultApi.md#streamsongstreamsongidget)                                                   | **GET** stream/{song_id}                  | Stream Song            |
| *DefaultApi* | [**uploadCoverUploadCoverPlaylistIdPost**](docs/DefaultApi.md#uploadcoveruploadcoverplaylistidpost)                             | **POST** upload/cover/{playlist_id}       | Upload Cover           |
| *DefaultApi* | [**uploadProfilePictureUploadProfilePictureUserIdPost**](docs/DefaultApi.md#uploadprofilepictureuploadprofilepictureuseridpost) | **POST** upload/profile-picture/{user_id} | Upload Profile Picture |

<a id="documentation-for-models"></a>

## Documentation for Models

- [de.hsb.vibeify.api.generated.models.HTTPValidationError](docs/HTTPValidationError.md)
- [de.hsb.vibeify.api.generated.models.ValidationError](docs/ValidationError.md)
- [de.hsb.vibeify.api.generated.models.ValidationErrorLocInner](docs/ValidationErrorLocInner.md)

<a id="documentation-for-authorization"></a>

## Documentation for Authorization

Endpoints do not require authorization.

