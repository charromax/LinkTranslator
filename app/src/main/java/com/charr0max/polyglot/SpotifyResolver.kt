package com.charr0max.polyglot

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Unofficial: reads title and artist from the public Spotify track page's Open Graph tags.
class SpotifyResolver : LinkResolver {
    override suspend fun resolve(url: String): TrackInfo? = withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            when (val code = conn.responseCode) {
                200 -> parseSpotifyPage(conn.inputStream.bufferedReader().readText())
                404 -> null
                else -> throw java.io.IOException("Spotify returned HTTP $code")
            }
        } finally {
            conn.disconnect()
        }
    }
}

internal fun parseSpotifyPage(html: String): TrackInfo? {
    fun meta(property: String) =
        Regex("""<meta property="$property" content="([^"]*)"""").find(html)?.groupValues?.get(1)?.let(::unescape)

    val title = meta("og:title")?.takeIf { it.isNotBlank() } ?: return null
    // og:description looks like "Artist · Album · Song · 2016".
    val artist = meta("og:description")?.substringBefore(" · ")?.takeIf { it.isNotBlank() }
    return TrackInfo(title, artist)
}

private fun unescape(s: String) = s
    .replace("&quot;", "\"").replace("&#x27;", "'").replace("&#39;", "'")
    .replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
