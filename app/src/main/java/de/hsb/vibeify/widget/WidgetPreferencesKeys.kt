import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey

object WidgetPreferencesKeys {
    val SONG_TITLE = stringPreferencesKey("song_title")
    val ARTIST = stringPreferencesKey("artist")
    val IS_PLAYING = booleanPreferencesKey("is_playing")
}
