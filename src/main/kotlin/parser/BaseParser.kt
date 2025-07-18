package net.keyfc.api.parser

import net.keyfc.api.ApiApplication
import net.keyfc.api.ext.doc
import net.keyfc.api.ext.plus
import net.keyfc.api.model.page.Breadcrumb
import net.keyfc.api.model.page.PageInfo
import net.keyfc.api.model.result.BaseParseResult
import org.jsoup.nodes.Document
import java.net.HttpCookie

abstract class BaseParser<T> {
    protected open fun parse(relativeUrl: String, cookies: List<HttpCookie>): T {
        validateUrl(relativeUrl)

        val url = ApiApplication.baseUrl + relativeUrl
        val baseParseResult = try {
            val doc = url.doc(cookies)
            BaseParseResult.Success(parsePageInfo(doc), parseBreadcrumbs(doc), doc)
        } catch (e: Exception) {
            BaseParseResult.Failure(e.message ?: "Unknown error occurred when parsing basic page info.", e)
        }

        return parse(baseParseResult)
    }

    protected abstract fun validateUrl(relativeUrl: String)

    protected abstract fun parse(baseParseResult: BaseParseResult): T

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

    /**
     * Parse breadcrumb navigation.
     *
     * Notice: for pages with no breadcrumbs, return an empty list.
     *
     * @return a list of breadcrumb navigation items
     */
    private fun parseBreadcrumbs(doc: Document): List<Breadcrumb> {
        val forumNav = doc.selectFirst("div.forumnav")
        return forumNav?.select("a")?.map {
            Breadcrumb(it.text().trim(), it.attr("href"))
        } ?: emptyList()
    }
}