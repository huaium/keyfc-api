# keyfc-api

![GitHub Tag](https://img.shields.io/github/v/tag/huaium/keyfc-api)

English | [中文](README_zh-cn.md)

> 🌟 This project has now become a Kotlin Multiplatform library.

A Kotlin Multiplatform library for parsing [KeyFC](https://keyfc.net/bbs/archiver/) pages into structured data classes.

## Supported

- [x] [Index (archiver)](https://keyfc.net/bbs/archiver/index.aspx)
- [x] [Forum (archiver)](https://keyfc.net/bbs/archiver/showforum-52.aspx)
- [x] [Topic (archiver)](https://keyfc.net/bbs/archiver/showtopic-70169.aspx)
- [x] [Login](https://keyfc.net/bbs/login.aspx)
- [x] [Search](https://keyfc.net/bbs/search.aspx)
- [x] [User center](https://keyfc.net/bbs/usercp.aspx)
- [x] [Notification](https://keyfc.net/bbs/usercpnotice.aspx?filter=all)
- [x] [Inbox](https://keyfc.net/bbs/usercpinbox.aspx)
- [x] [My topics](https://keyfc.net/bbs/mytopics.aspx)
- [x] [My posts](https://keyfc.net/bbs/myposts.aspx)
- [x] [My favourites](https://keyfc.net/bbs/usercpsubscribe.aspx)

## Installation

This library is not yet published to a public Maven repository. Only the first version is available on JitPack, which is
JVM and Android only.

To use it, you need to build and publish it to your
local Maven repository.

First clone this repository:

```shell
git clone https://github.com/huaium/keyfc-api.git
```

Build and publish the library to local Maven repository:

```shell
./gradlew :library:clean :library:publishToMavenLocal -x check
```

Switch to your project, and add the following content to `settings.gradle.kts` at the beginning of repositories:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        // other repositories...
    }
}
```

Then add the dependency to `build.gradle.kts` in your module:

```kotlin
dependencies {
    implementation("com.github.huaium:keyfc-api:<tag>")
}
```

## Example

Let's say you want to fetch data from the forum page with ID `52`:

```kotlin
val result: Result<ForumPage> =
    // Run in a coroutine scope, required by login and fetchForum
    runBlocking {
        KeyfcClient()
            // Assume you've got username and password as String
            .apply { login(username, password) }
            .use { it.fetchForum("52") }
    }

result.fold(
    onSuccess = { forumPage ->
        // Do something with the forum page
    },
    onFailure = { exception ->
        // Handle the exception
    }
)
```

For more, see [example](example).

## License

Please refer to [LICENSE](./LICENSE).