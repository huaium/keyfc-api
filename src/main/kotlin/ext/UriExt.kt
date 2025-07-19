package net.keyfc.api.ext

import java.net.URI

operator fun URI.plus(path: String): URI {
    val base = if (!this.toString().endsWith("/")) "$this/" else this.toString()
    return URI(base + path)
}