package net.keyfc.api

import okhttp3.OkHttpClient
import java.net.URI

object ApiApplication {
    val baseUri = URI("https://keyfc.net/bbs/") // `/` is needed for concatenation

    val archiverUri = URI("https://keyfc.net/bbs/archiver/")

    val loginUri = URI("https://keyfc.net/bbs/login.aspx")

    const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

    val httpClient = OkHttpClient()
}