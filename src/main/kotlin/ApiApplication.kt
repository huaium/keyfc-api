package net.keyfc.api

import okhttp3.OkHttpClient
import java.net.URL

object ApiApplication {
    val baseUrl = URL("https://keyfc.net/bbs/archiver/") // `/` is needed for concatenation

    const val LOGIN_URL = "https://keyfc.net/bbs/login.aspx"

    const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

    val httpClient = OkHttpClient()
}