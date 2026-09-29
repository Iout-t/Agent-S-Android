package com.agentx.android

import org.junit.Assert.assertEquals
import org.junit.Test

class AppTaskExecutorTest {
    @Test
    fun spotifySearchUsesHttpsContentLinkWithCleanQuery() {
        val query = "Alone by Marshmello"
        val expected = "https://open.spotify.com/search/Alone%20by%20Marshmello"

        assertEquals(expected, spotifyContentUrl(query))
        assertEquals("$query", TaskPlanner.plan("Open Spotify and play 'Alone' by Marshmello.").query)
    }
}
