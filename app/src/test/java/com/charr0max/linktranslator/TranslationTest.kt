package com.charr0max.linktranslator

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformAndParserTest {
    @Test fun lookupByKeyAndHost() {
        assertEquals(Platform.YouTubeMusic, Platform.fromId("youtubeMusic"))
        assertNull(Platform.fromId("nope"))
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
    private val track = TrackInfo("Song", "Band")
    private val songLink = "https://song.link/https://open.spotify.com/track/abc"

    private fun run(installed: Set<Platform>, preferred: Platform? = null, result: TrackInfo? = track, src: SourceLink = source) =
        runBlocking { translate(src, { result }, installed, preferred) }

    @Test fun singleTargetOpensSearch() {
        val o = run(setOf(Platform.Spotify, Platform.YouTubeMusic)) as Outcome.Open
        assertEquals(Platform.YouTubeMusic, o.option.platform)
        assertEquals("https://music.youtube.com/search?q=Song%20Band", o.option.url)
    }

    @Test fun multipleTargetsShowPicker() {
        val o = run(setOf(Platform.YouTubeMusic, Platform.Deezer)) as Outcome.Choose
        assertEquals(listOf(Platform.YouTubeMusic, Platform.Deezer), o.options.map { it.platform })
    }

    @Test fun sourceIsExcluded() {
        assertEquals(Outcome.Browser(songLink), run(setOf(Platform.Spotify)))
    }

    @Test fun noInstalledAppOpensSongLink() {
        assertEquals(Outcome.Browser(songLink), run(emptySet()))
    }

    @Test fun preferredSkipsPicker() {
        val o = run(setOf(Platform.YouTubeMusic, Platform.Deezer), preferred = Platform.Deezer) as Outcome.Open
        assertEquals(Platform.Deezer, o.option.platform)
    }

    @Test fun stalePreferenceFallsBackToPicker() {
        assertTrue(run(setOf(Platform.YouTubeMusic, Platform.Deezer), preferred = Platform.Tidal) is Outcome.Choose)
    }

    @Test fun unidentifiedTrackOpensSongLink() {
        assertEquals(Outcome.Browser(songLink), run(setOf(Platform.YouTubeMusic), result = null))
    }

    @Test fun nonSpotifySourceOpensSongLink() {
        val src = SourceLink("https://music.youtube.com/watch?v=1", Platform.YouTubeMusic)
        val o = run(setOf(Platform.Deezer), src = src)
        assertEquals(Outcome.Browser("https://song.link/https://music.youtube.com/watch?v=1"), o)
    }

    @Test(expected = java.io.IOException::class)
    fun unreachablePropagates() {
        runBlocking { translate(source, { throw java.io.IOException() }, setOf(Platform.Deezer), null) }
    }
}

class SpotifyParseTest {
    @Test fun parsesTitleAndArtist() {
        val html = """<meta property="og:title" content="Kitchen"/><meta property="og:description" content="Kid Cudi &middot; x"/>""".replace("&middot;", "·")
        assertEquals(TrackInfo("Kitchen", "Kid Cudi"), parseSpotifyPage(html))
    }

    @Test fun unescapesEntities() {
        val html = """<meta property="og:title" content="Rock &amp; Roll &#x27;n&#x27; Co"/>"""
        assertEquals("Rock & Roll 'n' Co", parseSpotifyPage(html)!!.title)
    }

    @Test fun missingTitleIsNull() {
        assertNull(parseSpotifyPage("<html></html>"))
    }
}

class TrackFinderTest {
    private val track = TrackInfo("Kitchen", "Kid Cudi")

    @Test fun deezerPicksMatchingResult() {
        val json = """{"data":[
            {"title":"Kitchen (Remix)","link":"https://deezer.com/track/9","artist":{"name":"Someone Else"}},
            {"title":"Kitchen","link":"https://www.deezer.com/track/1","artist":{"name":"Kid Cudi"}}]}"""
        assertEquals("https://www.deezer.com/track/1", parseDeezer(json, track))
    }

    @Test fun deezerNoMatchIsNull() {
        assertNull(parseDeezer("""{"data":[{"title":"Other","link":"x","artist":{"name":"Kid Cudi"}}]}""", track))
        assertNull(parseDeezer("""{"data":[]}""", track))
    }

    @Test fun itunesPicksMatchingResult() {
        val json = """{"results":[{"trackName":"Kitchen","artistName":"Kid Cudi","trackViewUrl":"https://music.apple.com/x?i=1"}]}"""
        assertEquals("https://music.apple.com/x?i=1", parseItunes(json, track))
    }

    @Test fun youTubeMusicPicksMatchingVideo() {
        fun item(id: String, title: String, sub: String) = """{"musicResponsiveListItemRenderer":{
            "playlistItemData":{"videoId":"$id"},
            "flexColumns":[
              {"musicResponsiveListItemFlexColumnRenderer":{"text":{"runs":[{"text":"$title"}]}}},
              {"musicResponsiveListItemFlexColumnRenderer":{"text":{"runs":[{"text":"$sub"}]}}}]}}"""
        val json = """{"contents":{"a":[${item("bad", "In Love", "Kid Cudi • Entergalactic • 3:39")},
            ${item("good", "Kitchen", "Kid Cudi • Passion, Pain & Demon Slayin' • 4:29")}]}}"""
        assertEquals("https://music.youtube.com/watch?v=good", parseYouTubeMusic(json, track))
        assertNull(parseYouTubeMusic("""{"contents":{}}""", track))
    }

    @Test fun matchingIgnoresCaseFeaturesAndSuffixes() {
        assertTrue(isMatch(TrackInfo("Ghost Town", "Kanye West"), "Ghost Town (feat. PARTYNEXTDOOR)", "Kanye West, Kid Cudi"))
        assertTrue(isMatch(TrackInfo("Song", "Band"), "SONG - Remastered 2011", "band"))
        assertTrue(!isMatch(TrackInfo("Song", "Band"), "Song", "Other"))
    }

    @Test fun matchesWhenSourceCreditsMoreArtistsThanTarget() {
        val t = TrackInfo("The Medicine Man [Feat. Old Crow Medicine Show]", "Keb' Mo', Old Crow Medicine Show")
        assertTrue(isMatch(t, "The Medicine Man (feat. Old Crow Medicine Show)", "Keb' Mo'"))
        assertTrue(!isMatch(t, "Medicine Man", "The Reeves Brothers"))
    }

    @Test fun refineFallsBackToSearchOnFailureOrNoMatch() {
        val option = Option(Platform.Deezer, "https://search")
        assertEquals(option, runBlocking { TrackFinder { _, _ -> null }.refine(option, track) })
        assertEquals(option, runBlocking { TrackFinder { _, _ -> throw java.io.IOException() }.refine(option, track) })
        assertEquals("https://direct", runBlocking { TrackFinder { _, _ -> "https://direct" }.refine(option, track).url })
    }
}
