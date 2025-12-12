package net.keyfc.api.parser

import com.fleeksoft.ksoup.Ksoup
import net.keyfc.api.ext.parseId
import kotlin.test.Test
import kotlin.test.assertEquals

internal class IndexParserTest {

    private val document = Ksoup.parse(sampleHtml)

    @Test
    fun `parseDocument builds the forum tree and page info`() {
        val indexPage = IndexParser.parseDocument(document)

        assertEquals("Sample Index", indexPage.pageInfo.title)
        assertEquals("kotlin,keyfc", indexPage.pageInfo.keywords)
        assertEquals("Example site index", indexPage.pageInfo.description)

        assertEquals(2, indexPage.categories.size)

        with(indexPage.categories.first()) {
            assertEquals("Category One", name)
            assertEquals("100", id)
            val rootForum = subForums.first()
            assertEquals("Forum Root", rootForum.name)
            assertEquals("200", rootForum.id)
            val nestedForum = rootForum.subForums.first()
            assertEquals("Forum Child", nestedForum.name)
            assertEquals("201", nestedForum.id)
        }

        with(indexPage.categories.last()) {
            assertEquals("Category Two", name)
            assertEquals("300", id)
            assertEquals(1, subForums.size)
            assertEquals("Another Forum", subForums.first().name)
        }
    }

    @Test
    fun `parseId extracts digits between hyphen and aspx`() {
        assertEquals("1234", "showtopic-1234.aspx".parseId())
        assertEquals("", "no-match".parseId())
    }
}

private const val sampleHtml = """
<!DOCTYPE html>
<html>
<head>
    <title>Sample Index</title>
    <meta name="keywords" content="kotlin,keyfc">
    <meta name="description" content="Example site index">
</head>
<body>
    <div class="cateitem">
        <h2><a href="showforum-100.aspx">Category One</a></h2>
    </div>

    <div class="forumitem">
        <h3><a href="showforum-200.aspx">Forum Root</a></h3>
    </div>

    <div class="forumitem">
        <h3>    <a href="showforum-201.aspx">Forum Child</a></h3>
    </div>

    <div class="cateitem">
        <h2><a href="showforum-300.aspx">Category Two</a></h2>
    </div>

    <div class="forumitem">
        <h3><a href="showforum-400.aspx">Another Forum</a></h3>
    </div>
</body>
</html>
"""
