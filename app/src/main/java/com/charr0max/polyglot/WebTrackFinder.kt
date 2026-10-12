package com.charr0max.polyglot

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

// Direct track lookups by title and artist. Deezer and iTunes are public APIs;
// the YouTube Music one is unofficial and may break, in which case callers fall back to search.
class WebTrackFinder : TrackFinder {
    override suspend fun find(platform: Platform, track: TrackInfo): String? = withContext(Dispatchers.IO) {
        val query = listOfNotNull(track.title, track.artist).joinToString(" ")
        val q = URLEncoder.encode(query, "UTF-8")
        when (platform) {
            Platform.Deezer -> parseDeezer(http("https://api.deezer.com/search/track?limit=5&q=$q"), track)
            Platform.AppleMusic -> parseItunes(http("https://itunes.apple.com/search?entity=song&limit=5&term=$q"), track)
            Platform.YouTubeMusic -> parseYouTubeMusic(youTubeMusicSearch(query), track)
            else -> null
        }
    }

    private fun http(url: String, body: String? = null): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 5_000
            conn.readTimeout = 5_000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            if (body != null) {
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.outputStream.use { it.write(body.toByteArray()) }
            }
            check(conn.responseCode == 200) { "HTTP ${conn.responseCode}" }
            return conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

    private fun youTubeMusicSearch(query: String): String {
        val body = JSONObject()
            .put("context", JSONObject().put("client", JSONObject().put("clientName", "WEB_REMIX").put("clientVersion", "1.20240101.01.00")))
            .put("query", query)
            .put("params", "EgWKAQIIAWoKEAMQBBAJEAoQBQ==") // songs filter
        return http("https://music.youtube.com/youtubei/v1/search?prettyPrint=false", body.toString())
    }
}

private fun normalize(s: String) = s.lowercase()
    .replace(Regex("""[(\[].*?[)\]]"""), " ")
    .substringBefore(" - ")
    .replace(Regex("""[^\p{L}\p{N}]+"""), " ")
    .trim()

internal fun isMatch(track: TrackInfo, title: String, artist: String): Boolean {
    val wantTitle = normalize(track.title)
    val gotTitle = normalize(title)
    val titleOk = wantTitle.isNotEmpty() && (wantTitle == gotTitle || gotTitle.startsWith(wantTitle))
    // Spotify lists every credited artist ("A, B") while other platforms often show only some of them.
    val wantArtists = track.artist.orEmpty().split(',', '&').map(::normalize).filter { it.isNotEmpty() }
    val gotArtist = normalize(artist)
    return titleOk && (wantArtists.isEmpty() || wantArtists.any { gotArtist.contains(it) })
}

internal fun parseDeezer(json: String, track: TrackInfo): String? {
    val data = JSONObject(json).optJSONArray("data") ?: return null
    return data.objects().firstOrNull {
        isMatch(track, it.optString("title"), it.optJSONObject("artist")?.optString("name").orEmpty())
    }?.optString("link")?.takeIf { it.isNotBlank() }
}

internal fun parseItunes(json: String, track: TrackInfo): String? {
    val results = JSONObject(json).optJSONArray("results") ?: return null
    return results.objects().firstOrNull {
        isMatch(track, it.optString("trackName"), it.optString("artistName"))
    }?.optString("trackViewUrl")?.takeIf { it.isNotBlank() }
}

internal fun parseYouTubeMusic(json: String, track: TrackInfo): String? {
    val items = mutableListOf<JSONObject>()
    fun walk(o: Any?) {
        when (o) {
            is JSONObject -> o.keys().forEach { k -> if (k == "musicResponsiveListItemRenderer") items += o.getJSONObject(k) else walk(o.get(k)) }
            is JSONArray -> for (i in 0 until o.length()) walk(o.get(i))
        }
    }
    walk(JSONObject(json))

    fun column(item: JSONObject, index: Int): String {
        val runs = item.optJSONArray("flexColumns")?.optJSONObject(index)
            ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")?.optJSONObject("text")?.optJSONArray("runs")
        return runs?.objects()?.joinToString("") { it.optString("text") }.orEmpty()
    }

    return items.firstNotNullOfOrNull { item ->
        val videoId = item.optJSONObject("playlistItemData")?.optString("videoId")?.takeIf { it.isNotBlank() }
        // The second column reads "Artist • Album • 4:29".
        if (videoId != null && isMatch(track, column(item, 0), column(item, 1).substringBefore(" • "))) {
            "https://music.youtube.com/watch?v=$videoId"
        } else null
    }
}

private fun JSONArray.objects() = (0 until length()).mapNotNull { optJSONObject(it) }
