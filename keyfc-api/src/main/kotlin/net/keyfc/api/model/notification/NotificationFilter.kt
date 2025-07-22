package net.keyfc.api.model.notification

enum class NotificationFilter(val value: String) {
    SPACE_COMMENT("spacecomment"),
    ALBUM_COMMENT("albumcomment"),
    POST_REPLY("postreply"),
    ALL("all"),
    TOPIC_ADMIN("topicadmin")
}