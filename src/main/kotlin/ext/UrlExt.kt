package net.keyfc.api.ext

import net.keyfc.api.ApiConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL

operator fun URL.plus(path: String): URL {
    val base = if (!this.toString().endsWith("/")) "$this/" else this.toString()
    return URL(base + path)
}

fun URL.doc(): Document {
    return Jsoup.connect(this.toString())
        .userAgent(ApiConfig.USER_AGENT)
        .get()
}