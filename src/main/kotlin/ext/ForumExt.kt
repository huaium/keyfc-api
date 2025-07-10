package net.keyfc.api.ext

import net.keyfc.api.model.page.index.Forum
import net.keyfc.api.parser.ForumParser

fun Forum.parse() {
    ForumParser(this).parse()
}