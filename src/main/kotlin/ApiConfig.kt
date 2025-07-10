package net.keyfc.api

import java.net.URL

object ApiConfig {
    val baseUrl: URL = URL("https://keyfc.net/bbs/archiver/") // `/` is needed for concatenation

    const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
}