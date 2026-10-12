package com.charr0max.linktranslator

data class TrackInfo(
    val links: Map<Platform, String>,
    val title: String?,
    val artist: String?,
    val pageUrl: String?,
)

fun interface LinkResolver {
    // Returns null when the service has no match; throws on network failure.
    suspend fun resolve(url: String): TrackInfo?
}

data class Option(val platform: Platform, val url: String)

sealed interface Outcome {
    data class Open(val option: Option) : Outcome
    data class Choose(val options: List<Option>) : Outcome
    data class Browser(val url: String) : Outcome
}

suspend fun translate(
    source: SourceLink,
    resolver: LinkResolver,
    installed: Set<Platform>,
    preferred: Platform?,
): Outcome {
    val track = resolver.resolve(source.url)
    val query = track?.let { listOfNotNull(it.title, it.artist).joinToString(" ").ifBlank { null } }

    val options = (installed - source.platform).sortedBy { it.ordinal }.mapNotNull { platform ->
        val url = track?.links?.get(platform) ?: query?.let(platform::searchUrl)
        url?.let { Option(platform, it) }
    }

    val direct = options.firstOrNull { it.platform == preferred && track?.links?.containsKey(it.platform) == true }
    return when {
        direct != null -> Outcome.Open(direct)
        options.size == 1 -> Outcome.Open(options.single())
        options.size > 1 -> Outcome.Choose(options)
        else -> Outcome.Browser(track?.pageUrl ?: source.url)
    }
}
