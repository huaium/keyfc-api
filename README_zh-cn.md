# keyfc-api

[![](https://jitpack.io/v/huaium/keyfc-api.svg)](https://jitpack.io/#huaium/keyfc-api)

[English](README.md) | 中文

> 🌟 本项目现已可用于 Kotlin Multiplatform。

用于解析 [KeyFC](https://keyfc.net/bbs/archiver/) 页面为结构化数据类的 Kotlin Multiplatform 库。

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

## 安装

在 `settings.gradle.kts` 文件中，将以下内容添加到 repositories 块的末尾：

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

然后添加依赖项到 `build.gradle.kts`：

```kotlin
dependencies {
    implementation("com.github.huaium:keyfc-api:Tag")
}
```

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

请查阅 [LICENSE](./LICENSE)。