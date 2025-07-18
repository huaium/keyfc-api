package net.keyfc.api.ext

import net.keyfc.api.ApiConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpCookie
import java.net.URL

operator fun URL.plus(path: String): URL {
    val base = if (!this.toString().endsWith("/")) "$this/" else this.toString()
    return URL(base + path)
}

fun URL.doc(cookies: List<HttpCookie>): Document {
    val cookieMap = cookies.associate { it.name to it.value }

    return Jsoup.connect(this.toString())
        .userAgent(ApiConfig.USER_AGENT)
        .cookies(cookieMap)
        .get()
}