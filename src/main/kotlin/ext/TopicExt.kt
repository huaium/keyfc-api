package net.keyfc.api.ext

import net.keyfc.api.model.page.forum.Topic
import net.keyfc.api.parser.TopicParser

fun Topic.parse() {
    TopicParser(this).parse()
}