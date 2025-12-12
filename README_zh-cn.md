# keyfc-api

![GitHub Tag](https://img.shields.io/github/v/tag/huaium/keyfc-api)

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

本仓库尚未发布至 Maven Central。仅首个版本可在 JitPack 获取，且只支持 JVM 和 Android。

因此，使用前需自行构建并发布至 Maven Local。

首先 clone 仓库：

```shell
git clone https://github.com/huaium/keyfc-api.git
```

构建版本并发布到 Maven Local：

```shell
./gradlew :library:clean :library:publishToMavenLocal -x check
```

在你的项目根目录下，添加以下内容到 `settings.gradle.kts` 的 repositories 块开头：

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        // other repositories...
    }
}
```

然后在 module 层面的 `build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    implementation("com.github.huaium:keyfc-api:<tag>")
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