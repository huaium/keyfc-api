# keyfc-api style and conventions
- Kotlin-first API uses namespaced packages under `net.keyfc.api` (e.g., `AuthClient`, `RepoClient`, `KeyfcClient`, plus `parser` and `model` subpackages) with PascalCase classes/objects and camelCase top-level functions and properties.
- Public API surfaces are documented with KDoc, especially the `KeyfcClient` operations; internal helpers rely on Kotlin `package`-level functions or objects placed in `parser`/`model` packages.
- Networking helpers are exposed as `suspend fun` that return `Result`-like data classes; `KeyfcClient` implements `AutoCloseable` and exposes read-only computed properties (`cookies`, `isLoggedIn`, `isLoggedInValid`) via getters.
- Parsers live in separate packages (`parser.*`) and keep responsibilities narrow, the client composes them through `RepoClient` and request cookies.
- Logging setup in samples uses Napier (`DebugAntilog`) before reading credentials; coroutine entry points typically use `runBlocking` to call `KeyfcClient` and `use` to scope the client lifecycle.
- Build scripts use Gradle Kotlin DSL with a version catalog (`libs.versions.toml`) and rely on consistent naming for dependencies (e.g., `libs.ktor.client.core`, `libs.ksoup`).
