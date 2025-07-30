# keyfc-api

[![](https://jitpack.io/v/huaium/keyfc-api.svg)](https://jitpack.io/#huaium/keyfc-api)

[English](README.md) | 中文

用于解析 [KeyFC](https://keyfc.net/bbs/archiver/) 页面为结构化数据类的库，为 Kotlin 和 Java 设计。

## 支持

- [x] [主页 (archiver)](https://keyfc.net/bbs/archiver/index.aspx)
- [x] [论坛 (archiver)](https://keyfc.net/bbs/archiver/showforum-52.aspx)
- [x] [帖子 (archiver)](https://keyfc.net/bbs/archiver/showtopic-70169.aspx)
- [x] [登录](https://keyfc.net/bbs/login.aspx)
- [x] [搜索](https://keyfc.net/bbs/search.aspx)
- [x] [用户中心](https://keyfc.net/bbs/usercp.aspx)
- [x] [通知](https://keyfc.net/bbs/usercpnotice.aspx?filter=all)
- [x] [收件箱](https://keyfc.net/bbs/usercpinbox.aspx)
- [x] [我的主题](https://keyfc.net/bbs/mytopics.aspx)
- [x] [我的帖子](https://keyfc.net/bbs/myposts.aspx)
- [x] [收藏夹](https://keyfc.net/bbs/usercpsubscribe.aspx)

## 用例

假设你想从ID为`52`的论坛页面获取数据：

```kotlin
val result: Result<ForumPage> =
    // login 和 fetchForum 需要在协程作用域中运行
    runBlocking {
        KeyfcClient()
            // 假设已有用户名和密码，且均为字符串
            .apply { login(username, password) }
            .use { it.fetchForum("52") }
    }

result.fold(
    onSuccess = { forumPage ->
        // 处理 forumPage
    },
    onFailure = { exception ->
        // 处理异常
    }
)
```

更多用例请查阅 [example](example)。

## 许可证

```
MIT License

Copyright (c) 2025 Huaium

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```