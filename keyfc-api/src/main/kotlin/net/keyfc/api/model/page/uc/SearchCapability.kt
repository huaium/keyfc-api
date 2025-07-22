package net.keyfc.api.model.page.uc

enum class SearchCapability(val code: Int, val description: String) {

    // From searchtype(data) function within the usercp page
    NOT_ALLOWED(0, "不允许"),
    TITLE_AND_CONTENT(1, "允许搜索标题或全文"),
    TITLE_ONLY(2, "仅允许搜索标题");

    companion object {
        fun fromCode(code: Int): SearchCapability? = entries.find { it.code == code }

        fun fromCodeOrDefault(code: Int, defaultType: SearchCapability = NOT_ALLOWED): SearchCapability {
            return fromCode(code) ?: defaultType
        }
    }
}