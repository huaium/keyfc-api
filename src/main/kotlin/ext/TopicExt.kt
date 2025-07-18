package net.keyfc.api.ext

import net.keyfc.api.model.page.forum.Topic
import net.keyfc.api.parser.TopicParser
import java.net.HttpCookie

fun Topic.parse(cookies: List<HttpCookie> = emptyList()) {
    TopicParser.parse(this, cookies)
}