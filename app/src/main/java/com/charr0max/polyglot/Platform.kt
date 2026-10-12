package com.charr0max.polyglot

import java.net.URI
import java.net.URLEncoder

enum class Platform(
    val id: String,
    val packageName: String,
    val displayName: String,
    val hosts: List<String>,
    private val searchTemplate: String,
) {
    Spotify("spotify", "com.spotify.music", "Spotify", listOf("open.spotify.com"), "https://open.spotify.com/search/{q}"),
    YouTubeMusic("youtubeMusic", "com.google.android.apps.youtube.music", "YouTube Music", listOf("music.youtube.com"), "https://music.youtube.com/search?q={q}"),
    Deezer("deezer", "deezer.android.app", "Deezer", listOf("deezer.com", "link.deezer.com"), "https://www.deezer.com/search/{q}"),
    Tidal("tidal", "com.aspiro.tidal", "Tidal", listOf("tidal.com"), "https://listen.tidal.com/search?q={q}"),
    AppleMusic("appleMusic", "com.apple.android.music", "Apple Music", listOf("music.apple.com"), "https://music.apple.com/search?term={q}");

    fun searchUrl(query: String): String =
        searchTemplate.replace("{q}", URLEncoder.encode(query, "UTF-8").replace("+", "%20"))

    companion object {
        fun fromId(id: String): Platform? = entries.firstOrNull { it.id == id }

        fun fromHost(host: String): Platform? = entries.firstOrNull { p ->
            p.hosts.any { host == it || host.endsWith(".$it") }
        }
    }
}

data class SourceLink(val url: String, val platform: Platform)

private val urlRegex = Regex("""https?://[^\s]+""")

fun parseLink(text: String): SourceLink? =
    urlRegex.findAll(text).firstNotNullOfOrNull { match ->
        val url = match.value.trimEnd('.', ',', ')', '>')
        val host = runCatching { URI(url).host }.getOrNull()?.lowercase() ?: return@firstNotNullOfOrNull null
        Platform.fromHost(host)?.let { SourceLink(url, it) }
    }
