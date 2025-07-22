package net.keyfc.api.model.page.uc

data class PostPermissions(
    val createTopics: Boolean,
    val replyToPosts: Boolean,
    val createPolls: Boolean,
    val voteInPolls: Boolean,
    val postRewards: Boolean,
    val postDebates: Boolean,
    val postTransactions: Boolean,
    val maxSignatureLength: Int,
    val useDiscuzCodeInSignature: Boolean,
    val useImgCodeInSignature: Boolean,
    val allowHtmlPosts: Boolean,
    val useHideCode: Boolean,
    val maxTopicPrice: Int
)