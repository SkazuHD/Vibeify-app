# Introductory manual 
# Mobile Computing - Vibeify

## Overview architecture
The entire project was built with the ModelView-ViewModel pattern in mind. We made sure to not mix UI elements with business logic. The viewmodels provide the UI state for the UI classes, which change their view according to the received data. 

To further separate the view from the Business Logic a service Layer was introduced which handles the communication with our Repositories.

Lastly a layer for our repositories was introduced, which would handle all the CRUD operations and provide usable data for the layers above.

Songs, Playlists and User Data  are persisted in Firestore. Additionally features such as the Live friend view is persisted in Firebase RealtimeDB. 

An additional API called RetroFit was introduced to handle the upload and fetching of our users profile pictures. These were stored on a separate server, for space and pricing reasons. Firebase was not a good fit for medium to large files. The mp3 files were stored on the separate server as well, with a link reference stored on Firebase.
For media playback we used androids media3 API.

## Login data
### Peter Lustig 

Email: peter@lustig.de

Password: lustig

### Flip Reset

Email: rl@pro.de

password: 123456A

## Manual

### App
To start the app, just sync the gradle and start the application.
The media player tends to fail, which is the result of the emulator failing. On an android mobile device, the playback works as intended.

Warning: The media player may break when used with the emulator provided by android studio, every feature still functions but you can no longer listen to the songs except when re-running the app
### Features
- Playlists can be shuffled and looped, songs in itself can also be looped
- Live Friends to see who is online and what they are listening to. Add friends to your following through the search.
- Playlists can be created and any songs can be inserted
- You can favorite songs from both the playback view and the playlist view.
- Favorited songs are automatically added to a personal playlist containing only your favorites.
- Recommendation will feature random songs to recommend
- Discovery will also feature random songs. These could in future be extended with recommendation logic. 
- Recent Activities, which will show the users recently listening to songs or playlists. It only shows unique entries.
- Search will show trending songs, genres, recommended playlists and a surprise section on default, and switches to search results upon entering a search term.
- Stickybar in the bottom can be used to control the mediaplayer, by forwarding, pausing/playing and stopping the player.
- In the Playlist Tab you can see playlists you created yourself, playlist you favorited and a “virtual” playlist containing your liked songs.
- Playlists can be created, liked, deleted and filled with songs through searching the song or adding it through the media player detail screen.
- The media player detail screen can be used to control the media player. It has the following options: play/ pause, forward, backwards, seeking to a certain time with the slider, liking the song, adding it to a playlist, shuffling the queue/playlist, looping  the song/playlist and looking at the queue
- Through the profile view, look at your created playlists, edit your profile name and picture, sign out, look at the people you follow and who are following you.



### Widget
- Long press on empty space on the home screen


- Tap Widgets


- Scroll down and find the app


- Tap and hold the widget


- Drag it to the home screen and release


### Features
- Stop and resume songs
- Previous song and next song buttons
- Pressing anywhere else on the widget leads you to the current songs playback view
