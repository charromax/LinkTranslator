package com.charr0max.polyglot

data class TrackInfo(val title: String, val artist: String?)

fun interface LinkResolver {
    // Returns null when the track can't be identified; throws on network failure.
    suspend fun resolve(url: String): TrackInfo?
}

data class Option(val platform: Platform, val url: String)

sealed interface Outcome {
    data class Open(val option: Option, val track: TrackInfo) : Outcome
    data class Choose(val options: List<Option>, val track: TrackInfo) : Outcome
    data class Browser(val url: String) : Outcome
}

fun songLinkUrl(sourceUrl: String) = "https://song.link/$sourceUrl"

suspend fun translate(
    source: SourceLink,
    resolver: LinkResolver,
    installed: Set<Platform>,
    preferred: Platform?,
): Outcome {
    val track = if (source.platform == Platform.Spotify) resolver.resolve(source.url) else null
    val fallback = Outcome.Browser(songLinkUrl(source.url))
    if (track == null) return fallback

    val query = listOfNotNull(track.title, track.artist).joinToString(" ")
    val options = (installed - source.platform).sortedBy { it.ordinal }.map { Option(it, it.searchUrl(query)) }

    return when {
        options.isEmpty() -> fallback
        options.size == 1 -> Outcome.Open(options.single(), track)
        else -> options.firstOrNull { it.platform == preferred }?.let { Outcome.Open(it, track) } ?: Outcome.Choose(options, track)
    }
}

fun interface TrackFinder {
    // Returns a direct track URL on the platform, or null when none is found.
    suspend fun find(platform: Platform, track: TrackInfo): String?
}

// Prefers a direct track link and keeps the search URL if the lookup fails or finds nothing.
suspend fun TrackFinder.refine(option: Option, track: TrackInfo): Option {
    val direct = try {
        find(option.platform, track)
    } catch (e: Exception) {
        if (e is kotlinx.coroutines.CancellationException) throw e
        null
    }
    return if (direct != null) option.copy(url = direct) else option
}
