package net.keyfc.api

import okhttp3.OkHttpClient
import java.net.URI

object ApiApplication {
    val baseUri = URI("https://keyfc.net/bbs/")

    const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

    val httpClient = OkHttpClient()
}