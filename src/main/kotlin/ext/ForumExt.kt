package net.keyfc.api.ext

import net.keyfc.api.model.page.index.Forum
import net.keyfc.api.parser.ForumParser
import java.net.HttpCookie

fun Forum.parse(cookies: List<HttpCookie> = emptyList()) {
    ForumParser.parse(this, cookies)
}