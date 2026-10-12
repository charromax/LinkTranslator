package com.charr0max.linktranslator

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

// Odesli (song.link) public API: unauthenticated and rate limited.
class OdesliResolver : LinkResolver {
    override suspend fun resolve(url: String): TrackInfo? = withContext(Dispatchers.IO) {
        val endpoint = "https://api.song.link/v1-alpha.1/links?url=" + URLEncoder.encode(url, "UTF-8")
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            when (val code = conn.responseCode) {
                200 -> parseOdesli(conn.inputStream.bufferedReader().readText())
                400, 404 -> null
                else -> throw java.io.IOException("Odesli returned HTTP $code")
            }
        } finally {
            conn.disconnect()
        }
    }
}

internal fun parseOdesli(json: String): TrackInfo {
    val root = JSONObject(json)
    val byPlatform = root.optJSONObject("linksByPlatform")
    val links = buildMap {
        byPlatform?.keys()?.forEach { key ->
            val platform = Platform.fromKey(key) ?: return@forEach
            byPlatform.getJSONObject(key).optString("url").takeIf { it.isNotBlank() }?.let { put(platform, it) }
        }
    }
    val entity = root.optJSONObject("entitiesByUniqueId")?.optJSONObject(root.optString("entityUniqueId"))
    return TrackInfo(
        links = links,
        title = entity?.optString("title")?.takeIf { it.isNotBlank() },
        artist = entity?.optString("artistName")?.takeIf { it.isNotBlank() },
        pageUrl = root.optString("pageUrl").takeIf { it.isNotBlank() },
    )
}
