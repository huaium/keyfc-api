package net.keyfc.api.model.uc

data class AttachmentPermissions(
    val downloadViewAttachments: Boolean,
    val uploadAttachments: Boolean,
    val setAttachmentPermissions: Boolean,
    val maxSingleAttachmentSize: Int,
    val maxDailyAttachmentSize: Int,
    val allowedAttachmentTypes: List<String>
)