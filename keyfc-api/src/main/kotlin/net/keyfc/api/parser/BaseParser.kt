package net.keyfc.api.parser

import com.fleeksoft.ksoup.nodes.Document
import net.keyfc.api.model.page.PageInfo
import net.keyfc.api.model.result.BaseParseResult

abstract class BaseParser {

    companion object {
        protected const val BASE_URL = "https://keyfc.net/bbs/" // `/` is needed
    }

    /**
     * Subclasses should call this function to parse the page.
     *
     * It will first parse basic information, and then return [BaseParseResult].
     */
    protected fun parseBase(document: Document): BaseParseResult {
        val baseParseResult = try {
            BaseParseResult.Success(
                document = document,
                pageInfo = parsePageInfo(document),
            )
        } catch (e: Exception) {
            BaseParseResult.Failure(e.message ?: "Unknown error occurred when parsing basic page info.", e)
        }

        return baseParseResult
    }

    /**
     * Parse basic page information.
     *
     * @return [PageInfo] with page title, keywords and description
     */
    private fun parsePageInfo(doc: Document): PageInfo {
        val title = doc.title()
        val keywords = doc.selectFirst("meta[name=keywords]")?.attr("content") ?: ""
        val description = doc.selectFirst("meta[name=description]")?.attr("content") ?: ""

        return PageInfo(title, keywords, description)
    }
}