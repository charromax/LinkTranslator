package com.charr0max.linktranslator

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformAndParserTest {
    @Test fun lookupByKeyAndHost() {
        assertEquals(Platform.YouTubeMusic, Platform.fromKey("youtubeMusic"))
        assertNull(Platform.fromKey("nope"))
        assertEquals(Platform.Spotify, Platform.fromHost("open.spotify.com"))
        assertEquals(Platform.Tidal, Platform.fromHost("listen.tidal.com"))
        assertNull(Platform.fromHost("example.com"))
    }

    @Test fun parsesBareUrl() {
        val link = parseLink("https://open.spotify.com/track/abc?si=1")!!
        assertEquals(Platform.Spotify, link.platform)
        assertEquals("https://open.spotify.com/track/abc?si=1", link.url)
    }

    @Test fun parsesUrlWithinText() {
        val link = parseLink("check this out https://example.com then https://open.spotify.com/track/abc. wow")!!
        assertEquals("https://open.spotify.com/track/abc", link.url)
    }

    @Test fun noLink() {
        assertNull(parseLink("just some text"))
        assertNull(parseLink("https://example.com/x"))
    }

    @Test fun searchUrlEncoding() {
        assertEquals("https://music.youtube.com/search?q=Daft%20Punk%20%26%20Co", Platform.YouTubeMusic.searchUrl("Daft Punk & Co"))
        assertEquals("https://www.deezer.com/search/a%20b", Platform.Deezer.searchUrl("a b"))
    }
}

class TranslateTest {
    private val source = SourceLink("https://open.spotify.com/track/abc", Platform.Spotify)
    private val track = TrackInfo(
        links = mapOf(
            Platform.Spotify to "https://open.spotify.com/track/abc",
            Platform.YouTubeMusic to "https://music.youtube.com/watch?v=1",
            Platform.Deezer to "https://deezer.com/track/1",
        ),
        title = "Song", artist = "Band", pageUrl = "https://song.link/s/abc",
    )

    private fun run(installed: Set<Platform>, preferred: Platform? = null, result: TrackInfo? = track) =
        runBlocking { translate(source, { result }, installed, preferred) }

    @Test fun singleTargetOpensDirectly() {
        val o = run(setOf(Platform.Spotify, Platform.YouTubeMusic)) as Outcome.Open
        assertEquals(Platform.YouTubeMusic, o.option.platform)
    }

    @Test fun multipleTargetsShowPicker() {
        val o = run(setOf(Platform.YouTubeMusic, Platform.Deezer)) as Outcome.Choose
        assertEquals(listOf(Platform.YouTubeMusic, Platform.Deezer), o.options.map { it.platform })
    }

    @Test fun sourceIsExcluded() {
        val o = run(setOf(Platform.Spotify)) as Outcome.Browser
        assertEquals("https://song.link/s/abc", o.url)
    }

    @Test fun noInstalledAppOpensBrowser() {
        assertTrue(run(emptySet()) is Outcome.Browser)
    }

    @Test fun preferredSkipsPicker() {
        val o = run(setOf(Platform.YouTubeMusic, Platform.Deezer), preferred = Platform.Deezer) as Outcome.Open
        assertEquals(Platform.Deezer, o.option.platform)
    }

    @Test fun stalePreferenceFallsBackToPicker() {
        val o = run(setOf(Platform.YouTubeMusic, Platform.Deezer), preferred = Platform.Tidal)
        assertTrue(o is Outcome.Choose)
    }

    @Test fun missingLinkUsesSearchFallback() {
        val partial = track.copy(links = mapOf(Platform.Spotify to source.url))
        val o = run(setOf(Platform.YouTubeMusic), result = partial) as Outcome.Open
        assertEquals("https://music.youtube.com/search?q=Song%20Band", o.option.url)
    }

    @Test fun noMatchOpensOriginalInBrowser() {
        val o = run(setOf(Platform.YouTubeMusic), result = null) as Outcome.Browser
        assertEquals(source.url, o.url)
    }

    @Test(expected = java.io.IOException::class)
    fun unreachablePropagates() {
        runBlocking { translate(source, { throw java.io.IOException() }, setOf(Platform.Deezer), null) }
    }
}

class OdesliParseTest {
    @Test fun parsesResponse() {
        val json = """
            {"entityUniqueId":"SPOTIFY_SONG::abc","pageUrl":"https://song.link/s/abc",
             "linksByPlatform":{"spotify":{"url":"https://open.spotify.com/track/abc"},
               "youtubeMusic":{"url":"https://music.youtube.com/watch?v=1"},
               "napster":{"url":"https://napster.com/x"}},
             "entitiesByUniqueId":{"SPOTIFY_SONG::abc":{"title":"Song","artistName":"Band"}}}
        """.trimIndent()
        val t = parseOdesli(json)
        assertEquals("Song", t.title)
        assertEquals("Band", t.artist)
        assertEquals("https://song.link/s/abc", t.pageUrl)
        assertEquals(setOf(Platform.Spotify, Platform.YouTubeMusic), t.links.keys)
    }
}
