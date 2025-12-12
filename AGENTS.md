# Repository Guidelines

## Project Structure & Module Organization
- `library` contains the Kotlin Multiplatform module with `commonMain`, platform-specific sources, and shared tests; platform compilations are configured in `library/build.gradle.kts`.
- `example` hosts a tiny JVM consumer that pulls `net.keyfc:api` from Maven for reference; inspect `example/src` for usage patterns.
- Root files (`build.gradle.kts`, `settings.gradle.kts`, `gradle/`, `gradlew`) orchestrate Gradle Settings, plugins, and the Kotlin multiplatform toolchain.
- Generated artifacts live under `build/`; avoid committing them and keep `.gitignore` aligned with published outputs.

## Build, Test, and Development Commands
- `./gradlew clean build` – runs the full multiplatform build, assembling every target and executing shared tests.
- `./gradlew :library:assemble` – produces the library artifacts for each platform; useful before publishing or running the example.
- `./gradlew :library:publishToMavenLocal` – installs the current snapshot to the local Maven cache for downstream testing without publishing.
- `./gradlew :example:build` – compiles the JVM example so you can inspect generated classes or run ad-hoc snippets via `java -jar`.

## Coding Style & Naming Conventions
- Follow idiomatic Kotlin conventions: 4-space indentation, `camelCase` for functions/variables, `PascalCase` for types, and uppercase constants where appropriate.
- Keep Kotlin files organized by source set (`commonMain`, `androidMain`, etc.) to reflect platform behavior.
- Rely on Gradle’s Kotlin DSL (`*.gradle.kts`) and prefer explicit `libs` catalog references for shared dependencies.
- Run `./gradlew spotlessApply`/`ktlint` only if those plugins are added later; otherwise rely on consistent formatting enforced by your IDE (Reformat Code → Kotlin style).

## Testing Guidelines
- Tests live under each source set’s `test` directory and use `kotlin.test` assertions to stay portable across targets.
- Name test classes with the `*Test` suffix and keep methods descriptive (e.g., `parseIndexPage_returnsIndex`).
- Execute `./gradlew test` or the broader `./gradlew clean build` to run every available test target; inspect `library/build/reports/tests` for results.

## Commit & Pull Request Guidelines
- Commits follow the `type: short description` pattern (see history: `test: add test for IndexParser and RepoClient`, `refactor: support kotlin multiplatform`).
- PRs should explain the change, link related issues or documentation, and include screenshots only if the change affects the UI; mention how to verify locally.
- Ensure PRs pass `./gradlew clean build` before requesting review and summarize the command output in the description or checks section.

## Example & Documentation Notes
- Reference `README.md` for installation and usage snippets, and browse `example/src/main/kotlin` for an idiomatic client call pattern.
- Update this guide or README whenever you introduce new modules, significant commands, or release requirements so contributors can stay aligned.
