package uc

import net.keyfc.api.result.parse.UcParseResult

fun printUc(result: UcParseResult) {
    when (result) {
        is UcParseResult.Success -> {
            val ucPage = result.ucPage

            println("\nTitle: ${ucPage.pageInfo.title}")
            println("Keywords: ${ucPage.pageInfo.keywords}")
            println("Description: ${ucPage.pageInfo.description}\n")

            println("User Information:")
            println("Username: ${ucPage.username}")
            println("Avatar URL: ${ucPage.avatar}")

            println("\nUser Stats:")
            val stats = ucPage.stats
            println("Score (积分): ${stats.score}")
            println("Experience (经验): ${stats.experience}")
            println("Popularity (人气): ${stats.popularity}")
            println("KP: ${stats.kp}")
            println("Credits (学分): ${stats.credits}")
            println("Good Person Cards (好人卡): ${stats.goodPersonCards}")
            println("Favorability (好感度): ${stats.favorability}")

            println("\nCounts:")
            println("Post Count: ${ucPage.postCount}")
            println("Digest Count: ${ucPage.digestCount}")
            println("New Message Count: ${ucPage.newMessageCount}")
            println("New Notice Count: ${ucPage.newNoticeCount}")

            println("\nSignature: ${ucPage.signature}")

            // Print permissions if available
            ucPage.permissions?.let { permissions ->
                println("\nUser Group:")
                val userGroup = permissions.userGroup
                println("  Name: ${userGroup.name}")
                println("  Level: ${userGroup.level}")
                println("  Type: ${userGroup.type}")
                println("  Starting Points: ${userGroup.startingPoints}")
                println("  Read Permission Level: ${userGroup.readPermissionLevel}")
                println("  Expiration Time: ${userGroup.expirationTime ?: "None"}")

                println("\nBasic Permissions:")
                val basicPerm = permissions.basicPermissions
                println("  Forum Access: ${basicPerm.forumAccess}")
                println("  Read Permission Level: ${basicPerm.readPermissionLevel}")
                println("  View User Profiles: ${basicPerm.viewUserProfiles}")
                println("  Search Capability: ${basicPerm.searchCapability}")
                println("  Message Inbox Capacity: ${basicPerm.messageInboxCapacity}")

                println("\nPost Permissions:")
                val postPerm = permissions.postPermissions
                println("  Create Topics: ${postPerm.createTopics}")
                println("  Reply To Posts: ${postPerm.replyToPosts}")
                println("  Create Polls: ${postPerm.createPolls}")
                println("  Vote In Polls: ${postPerm.voteInPolls}")
                println("  Post Rewards: ${postPerm.postRewards}")
                println("  Post Debates: ${postPerm.postDebates}")
                println("  Post Transactions: ${postPerm.postTransactions}")
                println("  Max Signature Length: ${postPerm.maxSignatureLength}")
                println("  Use Discuz Code In Signature: ${postPerm.useDiscuzCodeInSignature}")
                println("  Use Img Code In Signature: ${postPerm.useImgCodeInSignature}")
                println("  Allow HTML Posts: ${postPerm.allowHtmlPosts}")
                println("  Use Hide Code: ${postPerm.useHideCode}")
                println("  Max Topic Price: ${postPerm.maxTopicPrice}")

                println("\nAttachment Permissions:")
                val attachPerm = permissions.attachmentPermissions
                println("  Download/View Attachments: ${attachPerm.downloadViewAttachments}")
                println("  Upload Attachments: ${attachPerm.uploadAttachments}")
                println("  Set Attachment Permissions: ${attachPerm.setAttachmentPermissions}")
                println("  Max Single Attachment Size: ${attachPerm.maxSingleAttachmentSize}")
                println("  Max Daily Attachment Size: ${attachPerm.maxDailyAttachmentSize}")
                println("  Allowed Attachment Types: ${attachPerm.allowedAttachmentTypes}")
            } ?: println("\nNo permissions data available")
        }

        is UcParseResult.PermissionDenial -> {
            println("USER CONTROL PANEL ACCESS DENIED")
            println("Message: ${result.message}")
        }

        is UcParseResult.Failure -> {
            println("USER CONTROL PANEL ERROR")
            println("Message: ${result.message}")
            println("Exception: ${result.exception}")
        }
    }
}